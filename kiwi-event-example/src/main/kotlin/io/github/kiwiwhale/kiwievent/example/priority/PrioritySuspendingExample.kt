package io.github.kiwiwhale.kiwievent.example.priority

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.OrderCompleted
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main(): Unit = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<OrderCompleted>(priority = 1) {
        delay(100.milliseconds)
        println("Low priority: Send notification")
    }

    eventBus.subscribe<OrderCompleted>(priority = 10) {
        delay(100.milliseconds)
        println("High priority: Validate payment")
    }

    eventBus.subscribe<OrderCompleted>(priority = 5) {
        delay(100.milliseconds)
        println("Medium priority: Update inventory")
    }

    eventBus.publish(
        OrderCompleted(100, 1, 50000)
    )
}