package io.github.kiwiwhale.kiwievent.example.coroutines

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.OrderCompleted
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<OrderCompleted> {
        delay(100.milliseconds)
        println("Sending order confirmation")
    }

    eventBus.subscribe<OrderCompleted> {
        delay(100.milliseconds)
        println("Updating inventory")
    }

    eventBus.subscribe<OrderCompleted> {
        delay(100.milliseconds)
        println("Recording payment")
    }

    val startedAt = System.currentTimeMillis()

    eventBus.publish(
        OrderCompleted(100, 1, 50000)
    )

    val elapsed = System.currentTimeMillis() - startedAt

    println("Total elapsed time: ${elapsed}ms")
}