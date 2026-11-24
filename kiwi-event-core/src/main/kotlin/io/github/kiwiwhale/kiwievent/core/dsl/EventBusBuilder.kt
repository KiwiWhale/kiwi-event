package io.github.kiwiwhale.kiwievent.core.dsl

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.Subscription
import kotlin.reflect.KClass

@EventDsl
class EventBusBuilder internal constructor(
    private val bus: EventBus
) {

    fun <T : Any> on(
        type: KClass<T>,
        block: SubscriberBuilder<T>.() -> Unit
    ): Subscription {
        return bus.on(type, block)
    }

    fun build(): EventBus = bus
}

inline fun <reified T : Any> EventBusBuilder.on(
    noinline block: SubscriberBuilder<T>.() -> Unit
): Subscription {
    return on(T::class, block)
}

fun eventBus(
    block: EventBusBuilder.() -> Unit
): EventBus {
    val bus = EventBus.create()

    return EventBusBuilder(bus)
        .apply(block)
        .build()
}