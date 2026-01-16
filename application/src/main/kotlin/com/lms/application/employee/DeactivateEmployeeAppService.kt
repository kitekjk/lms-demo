package com.lms.application.employee

import com.lms.application.employee.dto.EmployeeResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근로자 비활성화 UseCase
 */
@Service
@Transactional
class DeactivateEmployeeAppService(private val employeeRepository: EmployeeRepository) {
    fun execute(context: DomainContext, employeeId: String): EmployeeResult {
        val employee = employeeRepository.findById(EmployeeId(employeeId))
            ?: throw EmployeeNotFoundException(employeeId)

        // 비활성화
        val deactivatedEmployee = employee.deactivate(context)

        // 저장
        val savedEmployee = employeeRepository.save(deactivatedEmployee)

        return EmployeeResult.from(savedEmployee)
    }
}
