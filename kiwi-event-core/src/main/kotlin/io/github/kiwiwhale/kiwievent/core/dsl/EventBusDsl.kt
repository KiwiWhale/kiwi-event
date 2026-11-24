package io.github.kiwiwhale.kiwievent.core.dsl

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.core.Subscription
import kotlin.reflect.KClass

fun <T : Any> EventBus.on(
    type: KClass<T>,
    block: SubscriberBuilder<T>.() -> Unit
): Subscription {
    val definition = SubscriberBuilder<T>()
        .apply(block)
        .build()

    return subscribe(
        type = type,
        priority = definition.priority,
        filter = definition.filter,
        handler = definition.handler
    )
}

inline fun <reified T : Any> EventBus.on(
    noinline block: SubscriberBuilder<T>.() -> Unit
): Subscription {
    return on(T::class, block)
}