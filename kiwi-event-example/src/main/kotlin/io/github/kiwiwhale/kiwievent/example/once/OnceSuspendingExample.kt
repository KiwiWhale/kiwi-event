package io.github.kiwiwhale.kiwievent.example.once

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main() = runBlocking {
    val eventBus = SuspendingEventBus()

    val subscription = eventBus.subscribe<UserCreated>(once = true) {
        println("Processing first user: ${it.username}")

        delay(300.milliseconds)

        println("First user processing completed")
    }

    repeat(3) { index ->
        eventBus.publish(
            UserCreated(index.toLong(), "user-$index")
        )
    }

    println("Subscription active: ${subscription.isActive}")
}