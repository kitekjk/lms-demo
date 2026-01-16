package com.lms.infrastructure.persistence.repository

import com.lms.domain.model.employee.Employee
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.user.UserId
import com.lms.infrastructure.persistence.entity.EmployeeEntity
import com.lms.infrastructure.persistence.mapper.EmployeeMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Employee JPA Repository
 */
@Repository
interface EmployeeJpaRepositoryInterface : JpaRepository<EmployeeEntity, String> {
    fun findByUserId(userId: String): EmployeeEntity?
    fun findByStoreId(storeId: String): List<EmployeeEntity>
    fun findByStoreIdAndIsActiveTrue(storeId: String): List<EmployeeEntity>
}

/**
 * Employee Repository 구현체
 * Domain의 EmployeeRepository 인터페이스를 구현
 */
@Repository
@Transactional
class EmployeeRepositoryImpl(private val jpaRepository: EmployeeJpaRepositoryInterface) : EmployeeRepository {

    override fun save(employee: Employee): Employee {
        val entity = EmployeeMapper.toEntity(employee)
        val saved = jpaRepository.save(entity)
        return EmployeeMapper.toDomain(saved)
    }

    override fun findById(employeeId: EmployeeId): Employee? = jpaRepository.findById(employeeId.value)
        .map { EmployeeMapper.toDomain(it) }
        .orElse(null)

    override fun findByUserId(userId: UserId): Employee? = jpaRepository.findByUserId(userId.value)
        ?.let { EmployeeMapper.toDomain(it) }

    override fun findByStoreId(storeId: StoreId): List<Employee> = jpaRepository.findByStoreId(storeId.value)
        .map { EmployeeMapper.toDomain(it) }

    override fun findActiveByStoreId(storeId: StoreId): List<Employee> =
        jpaRepository.findByStoreIdAndIsActiveTrue(storeId.value)
            .map { EmployeeMapper.toDomain(it) }

    override fun findAll(): List<Employee> = jpaRepository.findAll()
        .map { EmployeeMapper.toDomain(it) }

    override fun delete(employeeId: EmployeeId) {
        jpaRepository.deleteById(employeeId.value)
    }
}
