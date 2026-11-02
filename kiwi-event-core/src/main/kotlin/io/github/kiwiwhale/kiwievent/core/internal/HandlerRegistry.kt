package io.github.kiwiwhale.kiwievent.core.internal

import io.github.kiwiwhale.kiwievent.core.Subscription
import io.github.kiwiwhale.kiwievent.core.SubscriptionOptions
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

internal class HandlerRegistry<H> {

    private val lock = Any()
    private val sequence = AtomicLong()

    private val handlers =
        mutableMapOf<KClass<*>, LinkedHashMap<Long, HandlerEntry<H>>>()

    fun register(
        eventType: KClass<*>,
        options: SubscriptionOptions,
        handler: H
    ): Subscription {
        val entry = synchronized(lock) {
            HandlerEntry(
                id = sequence.incrementAndGet(),
                eventType = eventType,
                options = options,
                handler = handler
            ).also {
                handlers.getOrPut(eventType, ::linkedMapOf)[it.id] = it
            }
        }

        return object : Subscription {
            override val isActive: Boolean
                get() = entry.active.get()

            override fun cancel() {
                remove(entry)
            }
        }
    }

    fun snapshot(eventType: KClass<*>): List<HandlerEntry<H>> =
        synchronized(lock) {
            handlers[eventType]
                ?.values
                ?.sortedWith(
                    compareByDescending<HandlerEntry<H>> {
                        it.options.priority
                    }.thenBy {
                        it.id
                    }
                )
                .orEmpty()
        }

    fun claim(entry: HandlerEntry<H>): Boolean =
        synchronized(lock) {
            if (!entry.active.get()) {
                return@synchronized false
            }

            if (entry.options.once) {
                entry.active.set(false)
                removeLocked(entry)
            }

            true
        }

    private fun remove(entry: HandlerEntry<H>) {
        synchronized(lock) {
            if (!entry.active.compareAndSet(true, false)) {
                return
            }

            removeLocked(entry)
        }
    }

    private fun removeLocked(entry: HandlerEntry<H>) {
        handlers[entry.eventType]?.let { eventHandlers ->
            eventHandlers.remove(entry.id)

            if (eventHandlers.isEmpty()) {
                handlers.remove(entry.eventType)
            }
        }
    }

    fun clear(eventType: KClass<*>): Int =
        synchronized(lock) {
            val removed = handlers.remove(eventType)
                ?: return@synchronized 0

            removed.values.forEach {
                it.active.set(false)
            }

            removed.size
        }

    fun clear(): Int =
        synchronized(lock) {
            val count = handlers.values.sumOf { it.size }

            handlers.values.forEach { eventHandlers ->
                eventHandlers.values.forEach {
                    it.active.set(false)
                }
            }

            handlers.clear()
            count
        }

    fun subscriberCount(eventType: KClass<*>): Int =
        synchronized(lock) {
            handlers[eventType]?.size ?: 0
        }

    fun totalSubscriberCount(): Int =
        synchronized(lock) {
            handlers.values.sumOf { it.size }
        }
}