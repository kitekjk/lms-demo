package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.leave.LeaveRequest
import com.lms.domain.model.leave.LeaveRequestId
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.leave.LeaveStatus
import com.lms.infrastructure.persistence.entity.LeaveRequestEntity
import com.lms.infrastructure.persistence.mapper.LeaveRequestMapper
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LeaveRequestJpaRepository : JpaRepository<LeaveRequestEntity, String> {
    fun findByEmployeeId(employeeId: String): List<LeaveRequestEntity>
    fun findByEmployeeIdAndStatus(employeeId: String, status: String): List<LeaveRequestEntity>
    fun findByStatus(status: String): List<LeaveRequestEntity>

    @Query(
        """
        SELECT lr FROM LeaveRequestEntity lr
        WHERE lr.employeeId = :employeeId
        AND (lr.startDate <= :endDate AND lr.endDate >= :startDate)
    """
    )
    fun findByEmployeeIdAndDateRange(
        @Param("employeeId") employeeId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<LeaveRequestEntity>

    @Query(
        "SELECT l FROM LeaveRequestEntity l WHERE l.status = 'APPROVED' AND :targetDate BETWEEN l.startDate AND l.endDate"
    )
    fun findApprovedLeavesByDate(@Param("targetDate") targetDate: LocalDate): List<LeaveRequestEntity>

    @Query("SELECT l FROM LeaveRequestEntity l WHERE l.leaveType = :leaveType")
    fun findByLeaveType(@Param("leaveType") leaveType: String): List<LeaveRequestEntity>
}

@Repository
@Transactional
class LeaveRequestRepositoryImpl(private val jpaRepository: LeaveRequestJpaRepository) : LeaveRequestRepository {

    override fun save(leaveRequest: LeaveRequest): LeaveRequest {
        val entity = LeaveRequestMapper.toEntity(leaveRequest)
        val saved = jpaRepository.save(entity)
        return LeaveRequestMapper.toDomain(saved)
    }

    override fun findById(id: LeaveRequestId): LeaveRequest? = jpaRepository.findById(id.value)
        .map { LeaveRequestMapper.toDomain(it) }
        .orElse(null)

    override fun findByEmployeeId(employeeId: EmployeeId): List<LeaveRequest> =
        jpaRepository.findByEmployeeId(employeeId.value)
            .map { LeaveRequestMapper.toDomain(it) }

    override fun findByEmployeeIdAndStatus(employeeId: EmployeeId, status: LeaveStatus): List<LeaveRequest> =
        jpaRepository.findByEmployeeIdAndStatus(employeeId.value, status.name)
            .map { LeaveRequestMapper.toDomain(it) }

    override fun findByEmployeeIdAndDateRange(
        employeeId: EmployeeId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<LeaveRequest> = jpaRepository.findByEmployeeIdAndDateRange(
        employeeId.value,
        startDate,
        endDate
    ).map { LeaveRequestMapper.toDomain(it) }

    override fun findPendingRequests(): List<LeaveRequest> = jpaRepository.findByStatus(LeaveStatus.PENDING.name)
        .map { LeaveRequestMapper.toDomain(it) }

    override fun delete(id: LeaveRequestId) {
        jpaRepository.deleteById(id.value)
    }
}
