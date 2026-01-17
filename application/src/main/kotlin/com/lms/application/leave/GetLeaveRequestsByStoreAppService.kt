package com.lms.application.leave

import com.lms.application.leave.dto.LeaveRequestResult
import com.lms.domain.exception.StoreNotFoundException
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.leave.LeaveRequestRepository
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 매장별 휴가 신청 목록 조회 UseCase (관리자용)
 */
@Service
@Transactional(readOnly = true)
class GetLeaveRequestsByStoreAppService(
    private val leaveRequestRepository: LeaveRequestRepository,
    private val employeeRepository: EmployeeRepository,
    private val storeRepository: StoreRepository
) {
    fun execute(storeId: String): List<LeaveRequestResult> {
        val sId = StoreId.from(storeId)

        // 매장 존재 확인
        storeRepository.findById(sId)
            ?: throw StoreNotFoundException(storeId)

        // 해당 매장의 모든 근로자 조회
        val employees = employeeRepository.findByStoreId(sId)

        // 각 근로자의 휴가 신청 내역 조회
        val allLeaveRequests = employees.flatMap { employee ->
            leaveRequestRepository.findByEmployeeId(employee.id)
        }

        return allLeaveRequests.map { LeaveRequestResult.from(it) }
    }
}
