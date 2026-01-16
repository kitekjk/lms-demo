package com.lms.application.auth

import com.lms.application.auth.dto.RefreshTokenCommand
import com.lms.application.auth.dto.RefreshTokenResult
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.InvalidTokenException
import com.lms.domain.exception.TokenUserInactiveException
import com.lms.domain.exception.UserNotFoundException
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.user.UserId
import com.lms.domain.model.user.UserRepository
import com.lms.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 토큰 갱신 UseCase
 */
@Service
@Transactional(readOnly = true)
class RefreshTokenAppService(
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun execute(context: DomainContext, command: RefreshTokenCommand): RefreshTokenResult {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(command.refreshToken)) {
            throw InvalidTokenException()
        }

        // 2. Refresh Token에서 employeeId 추출
        val employeeId = jwtTokenProvider.extractEmployeeId(command.refreshToken)

        // 3. 사용자 조회 및 검증
        val user = userRepository.findById(UserId(employeeId))
            ?: throw UserNotFoundException()

        if (!user.isActive) {
            throw TokenUserInactiveException()
        }

        // 4. Employee 정보 조회 (storeId 가져오기 위해)
        val employee = employeeRepository.findByUserId(user.id)
        val storeId = employee?.storeId?.value

        // 5. 새로운 Access Token 생성
        val newAccessToken = jwtTokenProvider.generateAccessToken(
            employeeId = user.id.value,
            role = user.role.name,
            storeId = storeId
        )

        // 6. 응답 생성
        return RefreshTokenResult(accessToken = newAccessToken)
    }
}
