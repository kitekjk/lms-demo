package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.attendance.AttendanceRecord
import com.lms.domain.model.attendance.AttendanceRecordId
import com.lms.domain.model.attendance.AttendanceRecordRepository
import com.lms.domain.model.attendance.AttendanceStatus
import com.lms.domain.model.employee.EmployeeId
import com.lms.infrastructure.persistence.entity.AttendanceRecordEntity
import com.lms.infrastructure.persistence.mapper.AttendanceRecordMapper
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface AttendanceRecordJpaRepository : JpaRepository<AttendanceRecordEntity, String> {
    fun findByEmployeeId(employeeId: String): List<AttendanceRecordEntity>
    fun findByScheduleId(scheduleId: String): AttendanceRecordEntity?

    @Query(
        "SELECT a FROM AttendanceRecordEntity a WHERE a.employeeId = :employeeId AND DATE(a.checkInTime) = :workDate"
    )
    fun findByEmployeeIdAndWorkDate(
        @Param("employeeId") employeeId: String,
        @Param("workDate") workDate: LocalDate
    ): List<AttendanceRecordEntity>

    @Query(
        "SELECT a FROM AttendanceRecordEntity a WHERE a.employeeId = :employeeId AND DATE(a.checkInTime) BETWEEN :startDate AND :endDate"
    )
    fun findByEmployeeIdAndDateRange(
        @Param("employeeId") employeeId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE a.status = :status")
    fun findByStatus(@Param("status") status: String): List<AttendanceRecordEntity>

    fun findByEmployeeIdAndStatus(employeeId: String, status: String): List<AttendanceRecordEntity>

    @Query("SELECT a FROM AttendanceRecordEntity a WHERE a.checkOutTime IS NULL AND DATE(a.checkInTime) < CURRENT_DATE")
    fun findMissingCheckouts(): List<AttendanceRecordEntity>
}

@Repository
@Transactional
class AttendanceRecordRepositoryImpl(private val jpaRepository: AttendanceRecordJpaRepository) :
    AttendanceRecordRepository {

    override fun save(attendanceRecord: AttendanceRecord): AttendanceRecord {
        val entity = AttendanceRecordMapper.toEntity(attendanceRecord)
        val saved = jpaRepository.save(entity)
        return AttendanceRecordMapper.toDomain(saved)
    }

    override fun findById(id: AttendanceRecordId): AttendanceRecord? = jpaRepository.findById(id.value)
        .map { AttendanceRecordMapper.toDomain(it) }
        .orElse(null)

    override fun findByEmployeeId(employeeId: EmployeeId): List<AttendanceRecord> =
        jpaRepository.findByEmployeeId(employeeId.value)
            .map { AttendanceRecordMapper.toDomain(it) }

    override fun findByEmployeeIdAndDate(employeeId: EmployeeId, date: LocalDate): AttendanceRecord? =
        jpaRepository.findByEmployeeIdAndWorkDate(employeeId.value, date)
            .firstOrNull()
            ?.let { AttendanceRecordMapper.toDomain(it) }

    override fun findByEmployeeIdAndDateRange(
        employeeId: EmployeeId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> = jpaRepository.findByEmployeeIdAndDateRange(
        employeeId.value,
        startDate,
        endDate
    ).map { AttendanceRecordMapper.toDomain(it) }

    override fun findPendingByEmployeeId(employeeId: EmployeeId): List<AttendanceRecord> =
        jpaRepository.findByEmployeeIdAndStatus(
            employeeId.value,
            AttendanceStatus.PENDING.name
        ).map { AttendanceRecordMapper.toDomain(it) }

    override fun delete(id: AttendanceRecordId) {
        jpaRepository.deleteById(id.value)
    }
}
