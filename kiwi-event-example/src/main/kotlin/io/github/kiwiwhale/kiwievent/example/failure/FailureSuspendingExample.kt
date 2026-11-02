package io.github.kiwiwhale.kiwievent.example.failure

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

        println("Payment processing")

        error("Payment processing failed")
    }

    eventBus.subscribe<OrderCompleted> {
        delay(100.milliseconds)

        println("Inventory update")
    }

    eventBus.subscribe<OrderCompleted> {
        delay(100.milliseconds)

        println("Notification sent")
    }

    val result = eventBus.publish(
        OrderCompleted(100, 1, 50000)
    )

    println("Successful: ${result.successfulHandlers}")
    println("Failed: ${result.failedHandlers}")

    result.failures.forEach {
        println("Error: ${it.cause.message}")
    }
}