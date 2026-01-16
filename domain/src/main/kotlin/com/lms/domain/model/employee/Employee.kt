package com.lms.domain.model.employee

import com.lms.domain.common.DomainContext
import com.lms.domain.model.store.StoreId
import com.lms.domain.model.user.UserId
import java.math.BigDecimal
import java.time.Instant

/**
 * Employee Aggregate Root
 * 근로자 정보 관리 (순수 Kotlin)
 */
data class Employee private constructor(
    val id: EmployeeId,
    val userId: UserId, // User 1:1 관계
    val name: EmployeeName,
    val employeeType: EmployeeType,
    val storeId: StoreId?,
    val remainingLeave: RemainingLeave,
    val isActive: Boolean,
    val createdAt: Instant
) {
    companion object {
        /**
         * 새로운 근로자 생성
         */
        fun create(
            context: DomainContext,
            userId: UserId,
            name: EmployeeName,
            employeeType: EmployeeType,
            storeId: StoreId?
        ): Employee {
            // 직급별 초기 연차 설정
            val initialLeave = when (employeeType) {
                EmployeeType.REGULAR -> RemainingLeave(BigDecimal("15"))
                EmployeeType.IRREGULAR -> RemainingLeave(BigDecimal("11"))
                EmployeeType.PART_TIME -> RemainingLeave(BigDecimal.ZERO)
            }

            return Employee(
                id = EmployeeId.generate(),
                userId = userId,
                name = name,
                employeeType = employeeType,
                storeId = storeId,
                remainingLeave = initialLeave,
                isActive = true,
                createdAt = context.requestedAt
            )
        }

        /**
         * 기존 근로자 재구성
         */
        fun reconstruct(
            id: EmployeeId,
            userId: UserId,
            name: EmployeeName,
            employeeType: EmployeeType,
            storeId: StoreId?,
            remainingLeave: RemainingLeave,
            isActive: Boolean,
            createdAt: Instant
        ): Employee = Employee(
            id,
            userId,
            name,
            employeeType,
            storeId,
            remainingLeave,
            isActive,
            createdAt
        )
    }

    /**
     * 연차 차감 (휴가 승인 시)
     */
    fun deductLeave(context: DomainContext, days: BigDecimal): Employee {
        val newLeave = remainingLeave.deduct(days)
        return this.copy(remainingLeave = newLeave)
    }

    /**
     * 연차 복구 (휴가 취소 시)
     */
    fun restoreLeave(context: DomainContext, days: BigDecimal): Employee {
        val newLeave = remainingLeave.add(days)
        return this.copy(remainingLeave = newLeave)
    }

    /**
     * 매장 배정
     */
    fun assignStore(context: DomainContext, storeId: StoreId): Employee = this.copy(storeId = storeId)

    /**
     * 근로자 유형 변경
     */
    fun changeType(context: DomainContext, newType: EmployeeType): Employee = this.copy(employeeType = newType)

    /**
     * 비활성화
     */
    fun deactivate(context: DomainContext): Employee {
        require(isActive) { "이미 비활성화된 근로자입니다." }
        return this.copy(isActive = false)
    }

    /**
     * 활성화
     */
    fun activate(context: DomainContext): Employee {
        require(!isActive) { "이미 활성화된 근로자입니다." }
        return this.copy(isActive = true)
    }
}
