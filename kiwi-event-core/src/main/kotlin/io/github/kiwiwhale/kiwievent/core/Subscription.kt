package io.github.kiwiwhale.kiwievent.core

import java.util.concurrent.atomic.AtomicBoolean

class Subscription internal constructor(
    val id: Long,
    private val disposeAction: () -> Unit
) : AutoCloseable {

    private val disposed = AtomicBoolean(false)

    val isDisposed: Boolean
        get() = disposed.get()

    fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            disposeAction()
        }
    }

    internal fun disposeFromOwner() {
        disposed.set(true)
    }

    override fun close() {
        dispose()
    }
}
