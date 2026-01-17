package com.lms.infrastructure.context

/**
 * ThreadLocal을 사용하여 현재 스레드의 AuditContext를 저장
 */
object AuditContextHolder {
    private val contextHolder = ThreadLocal<AuditContext>()

    fun setContext(context: AuditContext) {
        contextHolder.set(context)
    }

    fun getContext(): AuditContext = contextHolder.get()
        ?: throw IllegalStateException("AuditContext가 설정되지 않았습니다")

    fun clear() {
        contextHolder.remove()
    }
}
