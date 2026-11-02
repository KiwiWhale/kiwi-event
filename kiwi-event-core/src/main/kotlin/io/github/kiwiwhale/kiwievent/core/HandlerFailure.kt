package io.github.kiwiwhale.kiwievent.core

data class HandlerFailure(
    val subscriptionId: Long,
    val cause: Exception
)