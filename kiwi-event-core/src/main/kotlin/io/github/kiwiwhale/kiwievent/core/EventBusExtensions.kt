package io.github.kiwiwhale.kiwievent.core

inline fun <reified E : Any> EventBus.subscribe(
    priority: Int = 0,
    once: Boolean = false,
    noinline handler: (E) -> Unit
): Subscription =
    subscribe(
        eventType = E::class,
        options = SubscriptionOptions(priority, once),
        handler = handler
    )

inline fun <reified E : Any> EventBus.subscriberCount(): Int =
    subscriberCount(E::class)

inline fun <reified E : Any> EventBus.unsubscribeAll(): Int =
    unsubscribeAll(E::class)