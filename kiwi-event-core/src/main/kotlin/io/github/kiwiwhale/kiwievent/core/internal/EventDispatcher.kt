package io.github.kiwiwhale.kiwievent.core.internal

import io.github.kiwiwhale.kiwievent.core.HandlerFailure
import io.github.kiwiwhale.kiwievent.core.PublishResult
import io.github.kiwiwhale.kiwievent.core.Subscription
import io.github.kiwiwhale.kiwievent.core.SubscriptionOptions
import java.util.concurrent.CancellationException
import kotlin.reflect.KClass

class EventDispatcher<H> {

    private val registry = HandlerRegistry<H>()

    private class DispatchSession<H>(
        private val registry: HandlerRegistry<H>,
        eventType: KClass<*>
    ) {
        val snapshot = registry.snapshot(eventType)

        var successfulHandlers = 0
            private set

        private val failures = mutableListOf<HandlerFailure>()

        fun claim(entry: HandlerEntry<H>): Boolean =
            registry.claim(entry)

        fun recordSuccess() {
            successfulHandlers++
        }

        fun recordFailure(
            entry: HandlerEntry<H>,
            exception: Exception
        ) {
            failures.add(
                HandlerFailure(
                    subscriptionId = entry.id,
                    cause = exception
                )
            )
        }

        fun result(eventType: KClass<*>): PublishResult =
            PublishResult(
                eventType = eventType.qualifiedName
                    ?: eventType.simpleName
                    ?: "Unknown",
                attemptedHandlers = snapshot.size,
                successfulHandlers = successfulHandlers,
                failures = failures.toList()
            )
    }

    fun register(
        eventType: KClass<*>,
        options: SubscriptionOptions,
        handler: H
    ): Subscription {
        return registry.register(eventType, options, handler)
    }

    fun dispatch(
        event: Any,
        eventType: KClass<*>,
        invoke: (H, Any) -> Unit
    ): PublishResult {
        val session = DispatchSession(registry, eventType)

        for (entry in session.snapshot) {
            if (!session.claim(entry)) continue

            try {
                invoke(entry.handler, event)
                session.recordSuccess()
            } catch (exception: Exception) {
                session.recordFailure(entry, exception)
            }
        }

        return session.result(eventType)
    }

    suspend fun dispatchSuspending(
        event: Any,
        eventType: KClass<*>,
        invoke: suspend (H, Any) -> Unit
    ): PublishResult {
        val session = DispatchSession(registry, eventType)

        for (entry in session.snapshot) {
            if (!session.claim(entry)) continue

            try {
                invoke(entry.handler, event)
                session.recordSuccess()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                session.recordFailure(entry, exception)
            }
        }

        return session.result(eventType)
    }

    fun subscriberCount(eventType: KClass<*>): Int =
        registry.subscriberCount(eventType)

    fun clear(eventType: KClass<*>): Int =
        registry.clear(eventType)

    fun clear(): Int = registry.clear()

    val totalSubscriberCount: Int
        get() = registry.totalSubscriberCount()
}