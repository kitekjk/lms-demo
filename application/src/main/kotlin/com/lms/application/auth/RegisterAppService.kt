package com.lms.application.auth

import com.lms.application.auth.dto.RegisterCommand
import com.lms.application.auth.dto.UserInfo
import com.lms.domain.common.DomainContext
import com.lms.domain.exception.DuplicateEmailException
import com.lms.domain.exception.InvalidRoleException
import com.lms.domain.model.user.Email
import com.lms.domain.model.user.Password
import com.lms.domain.model.user.Role
import com.lms.domain.model.user.User
import com.lms.domain.model.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원가입 UseCase
 * SUPER_ADMIN만 새로운 사용자를 등록할 수 있음
 */
@Service
@Transactional
class RegisterAppService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun execute(context: DomainContext, command: RegisterCommand): UserInfo {
        // 1. 이메일 중복 확인
        val email = Email(command.email)
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmailException(command.email)
        }

        // 2. 역할 검증
        val role = try {
            Role.valueOf(command.role)
        } catch (e: IllegalArgumentException) {
            throw InvalidRoleException(command.role)
        }

        // 3. 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(command.password)
        val password = Password(encodedPassword)

        // 4. 사용자 생성
        val user = if (role == Role.SUPER_ADMIN) {
            // SUPER_ADMIN은 create()를 사용할 수 없으므로 reconstruct() 사용
            User.reconstruct(
                id = com.lms.domain.model.user.UserId.generate(),
                email = email,
                password = password,
                role = role,
                isActive = true,
                createdAt = context.requestedAt,
                lastLoginAt = null
            )
        } else {
            User.create(context, email, password, role)
        }

        // 5. 저장
        val savedUser = userRepository.save(user)

        // 6. 응답 생성
        return UserInfo(
            userId = savedUser.id.value,
            email = savedUser.email.value,
            role = savedUser.role.name,
            isActive = savedUser.isActive
        )
    }
}
