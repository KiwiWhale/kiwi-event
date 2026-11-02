package io.github.kiwiwhale.kiwievent.example.synchronous

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    eventBus.subscribe<UserCreated> {
        error("Something went wrong")
    }

    eventBus.subscribe<UserCreated> {
        println("Second handler still executes")
    }

    val result = eventBus.publish(
        UserCreated(1, "kiwi")
    )

    println("Successful: ${result.successfulHandlers}")
    println("Failed: ${result.failedHandlers}")

    result.failures.forEach { failure ->
        println(failure.cause.message)
    }
}