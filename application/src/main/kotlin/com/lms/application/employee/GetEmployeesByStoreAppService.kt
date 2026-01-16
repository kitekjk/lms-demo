package com.lms.application.employee

import com.lms.application.employee.dto.EmployeeResult
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.store.StoreId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장별 근로자 목록 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetEmployeesByStoreAppService(private val employeeRepository: EmployeeRepository) {
    fun execute(storeId: String, activeOnly: Boolean = false): List<EmployeeResult> {
        val employees = if (activeOnly) {
            employeeRepository.findActiveByStoreId(StoreId(storeId))
        } else {
            employeeRepository.findByStoreId(StoreId(storeId))
        }
        return employees.map { EmployeeResult.from(it) }
    }
}
