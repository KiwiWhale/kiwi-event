package io.github.kiwiwhale.kiwievent.core

enum class FailurePhase {
    FILTER,
    HANDLER
}

data class EventFailure(
    val subscriptionId: Long,
    val eventType: String,
    val phase: FailurePhase,
    val exception: Exception
)
