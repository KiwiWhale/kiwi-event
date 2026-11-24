package io.github.kiwiwhale.kiwievent.core

internal class EventDispatcher(
    private val registry: EventRegistry
) {

    fun dispatch(event: Any): EventResult {
        val handlers = registry.find(event)
        val failures = mutableListOf<EventFailure>()

        var executedCount = 0
        var skippedCount = 0

        for (handler in handlers) {
            val matched = try {
                handler.filter(event)
            } catch (exception: Exception) {
                failures.add(
                    handler.toFailure(
                        phase = FailurePhase.FILTER,
                        exception = exception
                    )
                )

                skippedCount++
                continue
            }

            if (!matched) {
                skippedCount++
                continue
            }

            try {
                handler.action(event)
                executedCount++
            } catch (exception: Exception) {
                failures.add(
                    handler.toFailure(
                        phase = FailurePhase.HANDLER,
                        exception = exception
                    )
                )
            }
        }

        return EventResult(
            event = event,
            executedCount = executedCount,
            skippedCount = skippedCount,
            failures = failures.toList()
        )
    }

    private fun EventHandler.toFailure(
        phase: FailurePhase,
        exception: Exception
    ): EventFailure {
        return EventFailure(
            subscriptionId = id,
            eventType = eventType.qualifiedName
                ?: eventType.toString(),
            phase = phase,
            exception = exception
        )
    }
}