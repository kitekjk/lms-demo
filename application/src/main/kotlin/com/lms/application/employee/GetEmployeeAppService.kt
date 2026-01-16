package com.lms.application.employee

import com.lms.application.employee.dto.EmployeeResult
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근로자 단건 조회 UseCase
 */
@Service
@Transactional(readOnly = true)
class GetEmployeeAppService(private val employeeRepository: EmployeeRepository) {
    fun execute(employeeId: String): EmployeeResult? {
        val employee = employeeRepository.findById(EmployeeId(employeeId))
        return employee?.let { EmployeeResult.from(it) }
    }
}
