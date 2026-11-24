package io.github.kiwiwhale.kiwievent.core

import kotlin.reflect.KClass

internal class EventRegistry {

    private val lock = Any()

    private var sequence = 0L

    @Volatile
    private var closed = false

    @Volatile
    private var handlers: List<EventHandler> = emptyList()

    private val subscriptions = mutableMapOf<Long, Subscription>()

    val isClosed: Boolean
        get() = closed

    fun <T : Any> register(
        type: KClass<T>,
        priority: Int,
        filter: (T) -> Boolean,
        action: (T) -> Unit
    ): Subscription {
        synchronized(lock) {
            check(!closed) {
                "EventRegistry is already closed."
            }

            val id = ++sequence

            val subscription = Subscription(id) {
                unregister(id)
            }

            val handler = EventHandler(
                id = id,
                eventType = type,
                priority = priority,
                filter = { event ->
                    @Suppress("UNCHECKED_CAST")
                    filter(event as T)
                },
                action = { event ->
                    @Suppress("UNCHECKED_CAST")
                    action(event as T)
                }
            )

            handlers = (handlers + handler)
                .sortedWith(
                    compareByDescending<EventHandler> { it.priority }
                        .thenBy { it.id }
                )

            subscriptions[id] = subscription

            return subscription
        }
    }

    private fun unregister(id: Long) {
        synchronized(lock) {
            handlers = handlers.filterNot {
                it.id == id
            }

            subscriptions.remove(id)
        }
    }

    fun find(event: Any): List<EventHandler> {
        val eventClass = event::class.java
        val snapshot = handlers

        return buildList(snapshot.size) {
            for (handler in snapshot) {
                if (handler.eventType.java.isAssignableFrom(eventClass)) {
                    add(handler)
                }
            }
        }
    }

    fun clear() {
        synchronized(lock) {
            if (closed) {
                return
            }

            closed = true

            subscriptions.values.forEach {
                it.disposeFromOwner()
            }

            subscriptions.clear()
            handlers = emptyList()
        }
    }
}
