package io.github.kiwiwhale.kiwievent.example.subscription

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    val subscription = eventBus.subscribe<UserCreated> {
        println("Received: ${it.username}")
    }

    println("Active: ${subscription.isActive}")

    eventBus.publish(UserCreated(1, "kiwi"))

    subscription.cancel()

    println("Active: ${subscription.isActive}")

    eventBus.publish(UserCreated(2, "whale"))
}