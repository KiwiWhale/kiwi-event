package io.github.kiwiwhale.kiwievent.example.basic

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    eventBus.subscribe<UserCreated> { event ->
        println("User created: ${event.username}")
    }

    val result = eventBus.publish(
        UserCreated(1, "kiwi")
    )

    println("Successful handlers: ${result.successfulHandlers}")
}