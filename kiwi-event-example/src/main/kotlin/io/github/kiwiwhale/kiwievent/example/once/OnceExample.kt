package io.github.kiwiwhale.kiwievent.example.synchronous

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated

fun main() {
    val eventBus = EventBus()

    eventBus.subscribe<UserCreated>(once = true) {
        println("This handler executes only once")
    }

    repeat(3) { index ->
        eventBus.publish(
            UserCreated(index.toLong(), "user-$index")
        )
    }
}