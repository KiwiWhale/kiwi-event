package io.github.kiwiwhale.kiwievent.coroutines

import io.github.kiwiwhale.kiwievent.core.Subscription
import io.github.kiwiwhale.kiwievent.core.SubscriptionOptions

inline fun <reified E : Any> SuspendingEventBus.subscribe(
    priority: Int = 0,
    once: Boolean = false,
    noinline handler: suspend (E) -> Unit
): Subscription =
    subscribe(
        eventType = E::class,
        options = SubscriptionOptions(priority, once),
        handler = handler
    )

inline fun <reified E : Any> SuspendingEventBus.subscriberCount(): Int =
    subscriberCount(E::class)

inline fun <reified E : Any> SuspendingEventBus.unsubscribeAll(): Int =
    unsubscribeAll(E::class)