package io.github.kiwiwhale.kiwievent.core

import kotlin.reflect.KClass

internal data class EventHandler(
    val id: Long,
    val eventType: KClass<*>,
    val priority: Int,
    val filter: (Any) -> Boolean,
    val action: (Any) -> Unit
)