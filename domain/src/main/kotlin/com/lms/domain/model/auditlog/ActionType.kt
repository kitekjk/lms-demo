package com.lms.domain.model.auditlog

/**
 * 감사로그 액션 타입
 */
sealed class ActionType(val value: String) {
    data object Create : ActionType("CREATE")
    data object Update : ActionType("UPDATE")
    data object Delete : ActionType("DELETE")
    data object Activate : ActionType("ACTIVATE")
    data object Deactivate : ActionType("DEACTIVATE")

    companion object {
        fun from(value: String): ActionType = when (value) {
            "CREATE" -> Create
            "UPDATE" -> Update
            "DELETE" -> Delete
            "ACTIVATE" -> Activate
            "DEACTIVATE" -> Deactivate
            else -> throw IllegalArgumentException("Unknown ActionType: $value")
        }
    }
}
