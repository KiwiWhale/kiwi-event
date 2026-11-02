package io.github.kiwiwhale.kiwievent.core.internal

import io.github.kiwiwhale.kiwievent.core.SubscriptionOptions
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

internal class HandlerEntry<H>(
    val id: Long,
    val eventType: KClass<*>,
    val options: SubscriptionOptions,
    val handler: H
) {
    val active = AtomicBoolean(true)
}