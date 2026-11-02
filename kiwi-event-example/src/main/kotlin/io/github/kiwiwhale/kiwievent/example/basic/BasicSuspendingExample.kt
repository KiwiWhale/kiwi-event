package io.github.kiwiwhale.kiwievent.example.basic

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<UserCreated> { event ->
        println("Processing user: ${event.username}")

        delay(500.milliseconds)

        println("Welcome email sent")
    }

    val result = eventBus.publish(
        UserCreated(1, "kiwi")
    )

    println("Attempted handlers: ${result.attemptedHandlers}")
    println("Successful handlers: ${result.successfulHandlers}")
}