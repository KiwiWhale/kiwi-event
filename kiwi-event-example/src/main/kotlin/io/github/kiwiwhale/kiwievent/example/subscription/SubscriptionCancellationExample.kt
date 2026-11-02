package io.github.kiwiwhale.kiwievent.example.subscription

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

fun main(): Unit = runBlocking {
    val eventBus = SuspendingEventBus()

    val subscription = eventBus.subscribe<UserCreated> {
        println("Starting asynchronous processing")

        delay(300.milliseconds)

        println("Finished processing: ${it.username}")
    }

    eventBus.publish(UserCreated(1, "kiwi"))

    subscription.cancel()

    println("Subscription active: ${subscription.isActive}")

    eventBus.publish(UserCreated(2, "whale"))
}