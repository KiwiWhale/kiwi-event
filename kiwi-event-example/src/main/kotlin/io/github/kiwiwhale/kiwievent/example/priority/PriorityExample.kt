package io.github.kiwiwhale.kiwievent.example.synchronous

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main()  {
    val eventBus = EventBus()

    eventBus.subscribe<UserCreated>(priority = 1) {
        println("Low priority handler")
    }

    eventBus.subscribe<UserCreated>(priority = 10) {
        println("High priority handler")
    }

    eventBus.subscribe<UserCreated>(priority = 5) {
        println("Medium priority handler")
    }

    eventBus.publish(
        UserCreated(1, "kiwi")
    )
}