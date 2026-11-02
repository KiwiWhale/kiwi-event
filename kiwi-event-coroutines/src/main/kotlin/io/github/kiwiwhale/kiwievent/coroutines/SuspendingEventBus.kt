package io.github.kiwiwhale.kiwievent.coroutines

import io.github.kiwiwhale.kiwievent.core.PublishResult
import io.github.kiwiwhale.kiwievent.core.Subscription
import io.github.kiwiwhale.kiwievent.core.SubscriptionOptions
import io.github.kiwiwhale.kiwievent.core.internal.EventDispatcher
import kotlin.reflect.KClass

class SuspendingEventBus {

    private val dispatcher =
        EventDispatcher<suspend (Any) -> Unit>()

    fun <E : Any> subscribe(
        eventType: KClass<E>,
        options: SubscriptionOptions = SubscriptionOptions(),
        handler: suspend (E) -> Unit
    ): Subscription {
        val wrappedHandler: suspend (Any) -> Unit = { event ->
            @Suppress("UNCHECKED_CAST")
            handler(event as E)
        }

        return dispatcher.register(
            eventType = eventType,
            options = options,
            handler = wrappedHandler
        )
    }

    suspend fun publish(event: Any): PublishResult =
        dispatcher.dispatchSuspending(
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