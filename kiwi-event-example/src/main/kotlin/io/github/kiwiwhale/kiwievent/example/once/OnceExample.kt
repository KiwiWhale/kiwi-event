package io.github.kiwiwhale.kiwievent.example.once

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    val subscription = eventBus.subscribe<UserCreated>(once = true) {
        println("First user received: ${it.username}")
    }

    repeat(3) { index ->
        eventBus.publish(
            UserCreated(index.toLong(), "user-$index")
        )
    }

    println("Subscription active: ${subscription.isActive}")
}