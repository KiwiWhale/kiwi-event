package io.github.kiwiwhale.kiwievent.example.coroutines

import io.github.kiwiwhale.kiwievent.coroutines.SuspendingEventBus
import io.github.kiwiwhale.kiwievent.coroutines.subscribe
import io.github.kiwiwhale.kiwievent.example.event.UserCreated
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val eventBus = SuspendingEventBus()

    eventBus.subscribe<UserCreated> {
        println("First handler started")

        throw CancellationException("Operation cancelled")
    }

    eventBus.subscribe<UserCreated> {
        println("This handler will not execute")
    }

    try {
        eventBus.publish(UserCreated(1, "kiwi"))
    } catch (exception: CancellationException) {
        println("Publishing cancelled: ${exception.message}")
    }
}