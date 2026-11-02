package io.github.kiwiwhale.kiwievent.example.failure

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.OrderCompleted

fun main() {
    val eventBus = EventBus()

    eventBus.subscribe<OrderCompleted> {
        println("Payment processing")
        error("Payment processing failed")
    }

    eventBus.subscribe<OrderCompleted> {
        println("Inventory update")
    }

    eventBus.subscribe<OrderCompleted> {
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