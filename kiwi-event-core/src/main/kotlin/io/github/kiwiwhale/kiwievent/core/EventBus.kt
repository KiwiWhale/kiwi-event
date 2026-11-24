package io.github.kiwiwhale.kiwievent.core

import kotlin.reflect.KClass

class EventBus internal constructor(
    private val registry: EventRegistry
) : AutoCloseable {

    private val dispatcher = EventDispatcher(registry)

    val isClosed: Boolean
        get() = registry.isClosed

    fun emit(event: Any): EventResult {
        check(!isClosed) {
            "EventBus is already closed."
        }

        return dispatcher.dispatch(event)
    }

    fun <T : Any> subscribe(
        type: KClass<T>,
        priority: Int = 0,
        filter: (T) -> Boolean = { true },
        handler: (T) -> Unit
    ): Subscription {
        return registry.register(
            type = type,
            priority = priority,
            filter = filter,
            action = handler
        )
    }

    inline fun <reified T : Any> subscribe(
        priority: Int = 0,
        noinline filter: (T) -> Boolean = { true },
        noinline handler: (T) -> Unit
    ): Subscription {
        return subscribe(
            type = T::class,
            priority = priority,
            filter = filter,
            handler = handler
        )
    }

    override fun close() {
        registry.clear()
    }

    companion object {
        fun create(): EventBus {
            return EventBus(EventRegistry())
        }
    }
}