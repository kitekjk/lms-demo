package com.lms.domain.common

import java.time.Instant
import java.util.UUID

/**
 * 모든 도메인 요청의 공통 컨텍스트
 * HTTP, Kafka 등 외부 요청에서 추출한 메타데이터
 */
interface DomainContext {
    val serviceName: String // 요청 도메인/서비스명
    val userId: String // 사용자 ID
    val userName: String // 사용자 이름
    val roleId: String // 역할 ID (Role enum의 name)
    val requestId: String // 요청 추적용 ID (UUID, Trace ID 등)
    val requestedAt: Instant // 요청 시각
    val clientIp: String // 클라이언트 IP
}

/**
 * DomainContext 기본 구현체
 */
data class DomainContextBase(
    override val serviceName: String,
    override val userId: String,
    override val userName: String,
    override val roleId: String,
    override val requestId: String = UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
    override val clientIp: String
) : DomainContext
