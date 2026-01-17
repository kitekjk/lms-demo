package com.lms.application.schedule

import com.lms.application.schedule.dto.CreateWorkScheduleCommand
import com.lms.application.schedule.dto.WorkScheduleResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.DuplicateWorkScheduleException
import com.lms.domain.exception.EmployeeNotBelongToStoreException
import com.lms.domain.exception.EmployeeNotFoundException
import com.lms.domain.exception.StoreNotFoundException
import com.lms.domain.model.employee.EmployeeId
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.schedule.WorkDate
import com.lms.domain.model.schedule.WorkSchedule
import com.lms.domain.model.schedule.WorkScheduleRepository
import com.lms.domain.model.schedule.WorkTime
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 근무 일정 생성 UseCase
 */
@Service
@Transactional
class CreateWorkScheduleAppService(
    private val workScheduleRepository: WorkScheduleRepository,
    private val employeeRepository: EmployeeRepository,
    private val storeRepository: StoreRepository
) {
    fun execute(context: DomainContext, command: CreateWorkScheduleCommand): WorkScheduleResult {
        val employeeId = EmployeeId.from(command.employeeId)
        val storeId = StoreId.from(command.storeId)

        // 1. 근로자 존재 확인
        val employee = employeeRepository.findById(employeeId)
            ?: throw EmployeeNotFoundException(command.employeeId)

        // 2. 매장 존재 확인
        storeRepository.findById(storeId)
            ?: throw StoreNotFoundException(command.storeId)

        // 3. 근로자가 해당 매장에 속하는지 확인
        if (employee.storeId != storeId) {
            throw EmployeeNotBelongToStoreException(command.employeeId, command.storeId)
        }

        // 4. 중복 일정 확인 (동일 근로자, 동일 날짜)
        workScheduleRepository.findByEmployeeIdAndWorkDate(employeeId, command.workDate)?.let {
            throw DuplicateWorkScheduleException(command.employeeId, command.workDate.toString())
        }

        // 5. 근무 일정 생성
        val workSchedule = WorkSchedule.create(
            context = context,
            employeeId = employeeId,
            storeId = storeId,
            workDate = WorkDate(command.workDate),
            workTime = WorkTime(
                startTime = command.startTime,
                endTime = command.endTime
            )
        )

        // 6. 저장
        val savedSchedule = workScheduleRepository.save(workSchedule)

        return WorkScheduleResult.from(savedSchedule)
    }
}
