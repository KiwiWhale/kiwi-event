package io.github.kiwiwhale.kiwievent.example.nested

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.OrderCompleted
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    eventBus.subscribe<UserCreated> { event ->
        println("User registered: ${event.username}")

        eventBus.publish(
            OrderCompleted(
                orderId = 100,
                userId = event.userId,
                totalPrice = 50000
            )
        )
    }

    eventBus.subscribe<OrderCompleted> { event ->
        println("Order completed: ${event.orderId}")
    }

    eventBus.publish(
        UserCreated(1, "kiwi")
    )
}