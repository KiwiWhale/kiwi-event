package io.github.kiwiwhale.kiwievent.example.synchronous

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    val subscription = eventBus.subscribe<UserCreated> {
        println("Received user: ${it.username}")
    }

    eventBus.publish(
        UserCreated(1, "kiwi")
    )

    subscription.cancel()

    eventBus.publish(
        UserCreated(2, "whale")
    )

    println("Subscription active: ${subscription.isActive}")
}