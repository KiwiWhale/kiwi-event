package io.github.kiwiwhale.kiwievent.core.dsl

@EventDsl
class SubscriberBuilder<T : Any> {

    private var priority: Int = 0

    private var filter: (T) -> Boolean = { true }

    private var handler: ((T) -> Unit)? = null

    fun priority(value: Int) {
        priority = value
    }

    fun filter(predicate: (T) -> Boolean) {
        filter = predicate
    }

    fun handle(block: (T) -> Unit) {
        check(handler == null) {
            "A handler has already been defined."
        }

        handler = block
    }

    internal fun build(): SubscriberDefinition<T> {
        return SubscriberDefinition(
            priority = priority,
            filter = filter,
            handler = requireNotNull(handler) {
                "A handler must be defined."
            }
        )
    }
}

internal data class SubscriberDefinition<T : Any>(
    val priority: Int,
    val filter: (T) -> Boolean,
    val handler: (T) -> Unit
)