package io.github.kiwiwhale.kiwievent.example.nested

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.OrderCompleted
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main(): Unit = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<UserCreated> { event ->
        delay(100.milliseconds)

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
        delay(200.milliseconds)

        println("Order completed: ${event.orderId}")
    }

    eventBus.publish(
        UserCreated(1, "kiwi")
    )
}