package io.github.kiwiwhale.kiwievent.core

data class PublishResult(
    val eventType: String,
    val attemptedHandlers: Int,
    val successfulHandlers: Int,
    val failures: List<HandlerFailure>
) {

    val failedHandlers: Int
        get() = failures.size

    val dispatchedHandlers: Int
        get() = successfulHandlers + failedHandlers

    val skippedHandlers: Int
        get() = attemptedHandlers - dispatchedHandlers

    val isSuccess: Boolean
        get() = failures.isEmpty()

    val hasFailures: Boolean
        get() = failures.isNotEmpty()

    val wasHandled: Boolean
        get() = dispatchedHandlers > 0
}