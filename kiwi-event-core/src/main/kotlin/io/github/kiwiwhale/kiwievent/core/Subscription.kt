package io.github.kiwiwhale.kiwievent.core

interface Subscription : AutoCloseable {

    val isActive: Boolean

    fun cancel()

    override fun close() {
        cancel()
    }
}