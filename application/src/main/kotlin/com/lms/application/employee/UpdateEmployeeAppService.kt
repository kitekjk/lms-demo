package com.lms.application.employee

import com.lms.application.employee.dto.EmployeeResult
import com.lms.application.employee.dto.UpdateEmployeeCommand
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeName
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.store.StoreId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근로자 정보 수정 UseCase
 */
@Service
@Transactional
class UpdateEmployeeAppService(private val employeeRepository: EmployeeRepository) {
    fun execute(context: DomainContext, employeeId: String, command: UpdateEmployeeCommand): EmployeeResult {
        val employee = employeeRepository.findById(EmployeeId(employeeId))
            ?: throw EmployeeNotFoundException(employeeId)

        // 이름 변경
        var updatedEmployee = employee.copy(name = EmployeeName(command.name))

        // 근로자 유형 변경
        if (updatedEmployee.employeeType != command.employeeType) {
            updatedEmployee = updatedEmployee.changeType(context, command.employeeType)
        }

        // 매장 배정 변경
        val newStoreId = command.storeId?.let { StoreId(it) }
        if (updatedEmployee.storeId != newStoreId) {
            updatedEmployee = if (newStoreId != null) {
                updatedEmployee.assignStore(context, newStoreId)
            } else {
                updatedEmployee.copy(storeId = null)
            }
        }

        // 저장
        val savedEmployee = employeeRepository.save(updatedEmployee)

        return EmployeeResult.from(savedEmployee)
    }
}
