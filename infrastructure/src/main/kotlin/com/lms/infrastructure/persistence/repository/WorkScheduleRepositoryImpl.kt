package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.schedule.WorkSchedule
import com.lms.domain.model.schedule.WorkScheduleId
import com.lms.domain.model.schedule.WorkScheduleRepository
import com.lms.domain.model.store.StoreId
import com.lms.infrastructure.persistence.entity.WorkScheduleEntity
import com.lms.infrastructure.persistence.mapper.WorkScheduleMapper
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface WorkScheduleJpaRepository : JpaRepository<WorkScheduleEntity, String> {
    fun findByEmployeeId(employeeId: String): List<WorkScheduleEntity>
    fun findByStoreId(storeId: String): List<WorkScheduleEntity>
    fun findByWorkDate(workDate: LocalDate): List<WorkScheduleEntity>
    fun findByEmployeeIdAndWorkDate(employeeId: String, workDate: LocalDate): WorkScheduleEntity?
    fun findByStoreIdAndWorkDate(storeId: String, workDate: LocalDate): List<WorkScheduleEntity>

    @Query(
        "SELECT w FROM WorkScheduleEntity w WHERE w.employeeId = :employeeId AND w.workDate BETWEEN :startDate AND :endDate"
    )
    fun findByEmployeeIdAndWorkDateBetween(
        @Param("employeeId") employeeId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<WorkScheduleEntity>

    @Query(
        "SELECT w FROM WorkScheduleEntity w WHERE w.storeId = :storeId AND w.workDate BETWEEN :startDate AND :endDate"
    )
    fun findByStoreIdAndWorkDateBetween(
        @Param("storeId") storeId: String,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<WorkScheduleEntity>

    @Query(
        "SELECT w FROM WorkScheduleEntity w WHERE w.isConfirmed = true AND w.workDate BETWEEN :startDate AND :endDate"
    )
    fun findConfirmedSchedulesByDateRange(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<WorkScheduleEntity>
}

@Repository
@Transactional
class WorkScheduleRepositoryImpl(private val jpaRepository: WorkScheduleJpaRepository) : WorkScheduleRepository {

    override fun save(workSchedule: WorkSchedule): WorkSchedule {
        val entity = WorkScheduleMapper.toEntity(workSchedule)
        val saved = jpaRepository.save(entity)
        return WorkScheduleMapper.toDomain(saved)
    }

    override fun findById(id: WorkScheduleId): WorkSchedule? = jpaRepository.findById(id.value)
        .map { WorkScheduleMapper.toDomain(it) }
        .orElse(null)

    override fun findByEmployeeId(employeeId: EmployeeId): List<WorkSchedule> =
        jpaRepository.findByEmployeeId(employeeId.value)
            .map { WorkScheduleMapper.toDomain(it) }

    override fun findByStoreId(storeId: StoreId): List<WorkSchedule> = jpaRepository.findByStoreId(storeId.value)
        .map { WorkScheduleMapper.toDomain(it) }

    override fun findByEmployeeIdAndDateRange(
        employeeId: EmployeeId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WorkSchedule> = jpaRepository.findByEmployeeIdAndWorkDateBetween(
        employeeId.value,
        startDate,
        endDate
    ).map { WorkScheduleMapper.toDomain(it) }

    override fun findByStoreIdAndDateRange(
        storeId: StoreId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<WorkSchedule> = jpaRepository.findByStoreIdAndWorkDateBetween(
        storeId.value,
        startDate,
        endDate
    ).map { WorkScheduleMapper.toDomain(it) }

    override fun delete(id: WorkScheduleId) {
        jpaRepository.deleteById(id.value)
    }
}
