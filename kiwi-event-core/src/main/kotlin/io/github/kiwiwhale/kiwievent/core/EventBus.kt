package io.github.kiwiwhale.kiwievent.core

import io.github.kiwiwhale.kiwievent.core.internal.EventDispatcher
import kotlin.reflect.KClass

class EventBus {

    private val dispatcher =
        EventDispatcher<(Any) -> Unit>()

    fun <E : Any> subscribe(
        eventType: KClass<E>,
        options: SubscriptionOptions = SubscriptionOptions(),
        handler: (E) -> Unit
    ): Subscription {
        val wrappedHandler: (Any) -> Unit = { event ->
            @Suppress("UNCHECKED_CAST")
            handler(event as E)
        }

        return dispatcher.register(
            eventType = eventType,
            options = options,
            handler = wrappedHandler
        )
    }

    fun publish(event: Any): PublishResult =
        dispatcher.dispatch(
            event = event,
            eventType = event::class
        ) { handler, publishedEvent ->
            handler(publishedEvent)
        }

    fun <E : Any> subscriberCount(eventType: KClass<E>): Int =
        dispatcher.subscriberCount(eventType)

    fun <E : Any> unsubscribeAll(eventType: KClass<E>): Int =
        dispatcher.clear(eventType)

    fun clear(): Int =
        dispatcher.clear()

    val totalSubscriberCount: Int
        get() = dispatcher.totalSubscriberCount
}