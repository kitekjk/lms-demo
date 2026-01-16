package com.lms.infrastructure.security

import com.lms.domain.model.user.Email
import com.lms.domain.model.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Spring Security UserDetailsService 구현
 * 이메일로 사용자 정보 로드
 */
@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    /**
     * 이메일로 사용자 정보 조회
     * @param username 이메일 주소
     * @return UserDetails
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(Email(username))
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")

        return User.builder()
            .username(user.id.value)
            .password(user.password.encodedValue)
            .authorities(listOf(SimpleGrantedAuthority("ROLE_${user.role.name}")))
            .accountExpired(false)
            .accountLocked(!user.isActive)
            .credentialsExpired(false)
            .disabled(!user.isActive)
            .build()
    }
}
