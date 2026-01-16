package com.lms.application.employee

import com.lms.application.employee.dto.EmployeeResult
import com.lms.domain.model.employee.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 전체 근로자 목록 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetAllEmployeesAppService(private val employeeRepository: EmployeeRepository) {
    fun execute(): List<EmployeeResult> {
        val employees = employeeRepository.findAll()
        return employees.map { EmployeeResult.from(it) }
    }
}
