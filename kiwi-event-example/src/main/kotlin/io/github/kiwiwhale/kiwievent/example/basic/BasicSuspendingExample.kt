package io.github.kiwiwhale.kiwievent.example.coroutines

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<UserCreated> { event ->
        delay(100.milliseconds)

        println("Sending welcome email to ${event.username}")
    }

    val result = eventBus.publish(
        UserCreated(1, "kiwi")
    )

    println("Successful handlers: ${result.successfulHandlers}")
}