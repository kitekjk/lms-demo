package com.lms.application.auth

import com.lms.application.auth.dto.LoginCommand
import com.lms.application.auth.dto.LoginResult
import com.lms.application.auth.dto.UserInfo
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.AuthenticationFailedException
import com.lms.domain.exception.InactiveUserException
import com.lms.domain.model.employee.EmployeeRepository
import com.lms.domain.model.user.Email
import com.lms.domain.model.user.UserRepository
import com.lms.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 로그인 UseCase
 */
@Service
@Transactional
class LoginAppService(
    private val userRepository: UserRepository,
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun execute(context: DomainContext, command: LoginCommand): LoginResult {
        // 1. 이메일로 사용자 조회
        val user = userRepository.findByEmail(Email(command.email))
            ?: throw AuthenticationFailedException()

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(command.password, user.password.encodedValue)) {
            throw AuthenticationFailedException()
        }

        // 3. 사용자 활성화 상태 확인
        if (!user.isActive) {
            throw InactiveUserException()
        }

        // 4. 로그인 처리 (lastLoginAt 업데이트)
        val loggedInUser = user.login(context)
        userRepository.save(loggedInUser)

        // 5. Employee 정보 조회 (storeId 가져오기 위해)
        val employee = employeeRepository.findByUserId(user.id)
        val storeId = employee?.storeId?.value

        // 6. JWT 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(
            employeeId = user.id.value,
            role = user.role.name,
            storeId = storeId
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(
            employeeId = user.id.value
        )

        // 7. 응답 생성
        return LoginResult(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userInfo = UserInfo(
                userId = user.id.value,
                email = user.email.value,
                role = user.role.name,
                isActive = user.isActive
            )
        )
    }
}
