package com.lms.application.employee

import com.lms.application.employee.dto.CreateEmployeeCommand
import com.lms.application.employee.dto.EmployeeResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.DuplicateEmployeeUserException
import com.lms.domain.model.employee.Employee
import com.lms.domain.model.employee.EmployeeName
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근로자 생성 UseCase
 */
@Service
@Transactional
class CreateEmployeeAppService(private val employeeRepository: EmployeeRepository) {
    fun execute(context: DomainContext, command: CreateEmployeeCommand): EmployeeResult {
        val userId = UserId(command.userId)

        // 중복 사용자 검증
        employeeRepository.findByUserId(userId)?.let {
            throw DuplicateEmployeeUserException(command.userId)
        }

        // 근로자 생성
        val employee = Employee.create(
            context = context,
            userId = userId,
            name = EmployeeName(command.name),
            employeeType = command.employeeType,
            storeId = command.storeId?.let { StoreId(it) }
        )

        // 저장
        val savedEmployee = employeeRepository.save(employee)

        return EmployeeResult.from(savedEmployee)
    }
}
