package io.github.kiwiwhale.kiwievent.example.coroutines

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

fun main(): Unit = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<UserCreated> {
        println("Starting user processing")

        delay(1000.milliseconds)

        println("User processing completed")
    }

    try {
        withTimeout(300.milliseconds) {
            eventBus.publish(
                UserCreated(1, "kiwi")
            )
        }
    } catch (exception: TimeoutCancellationException) {
        println("Event processing timed out")
    }
}