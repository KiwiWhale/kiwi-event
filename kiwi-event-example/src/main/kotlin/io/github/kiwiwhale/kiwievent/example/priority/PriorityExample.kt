package io.github.kiwiwhale.kiwievent.example.priority

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.OrderCompleted

fun main() {
    val eventBus = EventBus()

    eventBus.subscribe<OrderCompleted>(priority = 1) {
        println("Low priority: Send notification")
    }

    eventBus.subscribe<OrderCompleted>(priority = 10) {
        println("High priority: Validate payment")
    }

    eventBus.subscribe<OrderCompleted>(priority = 5) {
        println("Medium priority: Update inventory")
    }

    eventBus.publish(
        OrderCompleted(100, 1, 50000)
    )
}