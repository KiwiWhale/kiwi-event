package io.github.kiwiwhale.kiwievent.core

data class EventResult(
    val event: Any,
    val executedCount: Int,
    val skippedCount: Int,
    val failures: List<EventFailure>
) {
    val isSuccess: Boolean
        get() = failures.isEmpty()

    val hasFailures: Boolean
        get() = failures.isNotEmpty()
}
