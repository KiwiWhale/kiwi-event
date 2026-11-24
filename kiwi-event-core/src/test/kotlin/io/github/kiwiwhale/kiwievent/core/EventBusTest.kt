package io.github.kiwiwhale.kiwievent.core

import io.github.kiwiwhale.kiwievent.core.dsl.eventBus
import io.github.kiwiwhale.kiwievent.core.dsl.on
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class EventBusTest {

    data class UserCreated(
        val id: Long,
        val active: Boolean = true
    )

    data class UserDeleted(
        val id: Long
    )

    open class BaseEvent

    class ChildEvent : BaseEvent()

    @Test
    fun `구독한 이벤트를 정상적으로 발행한다`() {
        var received: UserCreated? = null

        val bus = eventBus {
            on<UserCreated> {
                handle {
                    received = it
                }
            }
        }

        val event = UserCreated(1)
        val result = bus.emit(event)

        assertEquals(event, received)
        assertEquals(1, result.executedCount)
        assertEquals(0, result.skippedCount)
        assertTrue(result.isSuccess)

        bus.close()
    }

    @Test
    fun `관련 없는 이벤트는 구독자에게 전달하지 않는다`() {
        var executed = false

        val bus = eventBus {
            on<UserCreated> {
                handle {
                    executed = true
                }
            }
        }

        val result = bus.emit(UserDeleted(1))

        assertFalse(executed)
        assertEquals(0, result.executedCount)
        assertTrue(result.isSuccess)

        bus.close()
    }

    @Test
    fun `부모 이벤트 타입으로 자식 이벤트를 구독할 수 있다`() {
        var executed = false

        val bus = eventBus {
            on<BaseEvent> {
                handle {
                    executed = true
                }
            }
        }

        bus.emit(ChildEvent())

        assertTrue(executed)

        bus.close()
    }

    @Test
    fun `동일한 이벤트에 여러 핸들러를 등록할 수 있다`() {
        val executionOrder = mutableListOf<Int>()

        val bus = eventBus {
            repeat(3) { index ->
                on<UserCreated> {
                    handle {
                        executionOrder += index
                    }
                }
            }
        }

        val result = bus.emit(UserCreated(1))

        assertEquals(listOf(0, 1, 2), executionOrder)
        assertEquals(3, result.executedCount)

        bus.close()
    }

    @Test
    fun `우선순위가 높은 핸들러부터 실행한다`() {
        val executionOrder = mutableListOf<Int>()

        val bus = eventBus {
            on<UserCreated> {
                priority(0)
                handle { executionOrder += 0 }
            }

            on<UserCreated> {
                priority(10)
                handle { executionOrder += 10 }
            }

            on<UserCreated> {
                priority(5)
                handle { executionOrder += 5 }
            }
        }

        bus.emit(UserCreated(1))

        assertEquals(listOf(10, 5, 0), executionOrder)

        bus.close()
    }

    @Test
    fun `동일한 우선순위에서는 등록 순서를 유지한다`() {
        val executionOrder = mutableListOf<Int>()

        val bus = eventBus {
            repeat(5) { index ->
                on<UserCreated> {
                    priority(10)
                    handle {
                        executionOrder += index
                    }
                }
            }
        }

        bus.emit(UserCreated(1))

        assertEquals(listOf(0, 1, 2, 3, 4), executionOrder)

        bus.close()
    }

    @Test
    fun `필터 조건을 만족하는 이벤트만 실행한다`() {
        var executed = false

        val bus = eventBus {
            on<UserCreated> {
                filter { it.active }

                handle {
                    executed = true
                }
            }
        }

        val result = bus.emit(UserCreated(1, active = false))

        assertFalse(executed)
        assertEquals(0, result.executedCount)
        assertEquals(1, result.skippedCount)

        bus.close()
    }

    @Test
    fun `필터에서 예외가 발생해도 다음 핸들러를 실행한다`() {
        var executed = false

        val bus = eventBus {
            on<UserCreated> {
                filter {
                    throw IllegalStateException("Filter failed")
                }

                handle {}
            }

            on<UserCreated> {
                handle {
                    executed = true
                }
            }
        }

        val result = bus.emit(UserCreated(1))

        assertTrue(executed)
        assertEquals(1, result.executedCount)
        assertEquals(1, result.skippedCount)
        assertEquals(1, result.failures.size)
        assertEquals(FailurePhase.FILTER, result.failures.first().phase)

        bus.close()
    }

    @Test
    fun `핸들러에서 예외가 발생해도 다음 핸들러를 실행한다`() {
        var executed = false

        val bus = eventBus {
            on<UserCreated> {
                handle {
                    throw IllegalStateException("Handler failed")
                }
            }

            on<UserCreated> {
                handle {
                    executed = true
                }
            }
        }

        val result = bus.emit(UserCreated(1))

        assertTrue(executed)
        assertEquals(1, result.executedCount)
        assertEquals(1, result.failures.size)
        assertEquals(FailurePhase.HANDLER, result.failures.first().phase)

        bus.close()
    }

    @Test
    fun `실행 결과의 통계를 정확하게 반환한다`() {
        val bus = eventBus {
            on<UserCreated> {
                handle {}
            }

            on<UserCreated> {
                filter { false }
                handle {}
            }

            on<UserCreated> {
                handle {
                    throw IllegalStateException()
                }
            }
        }

        val result = bus.emit(UserCreated(1))

        assertEquals(1, result.executedCount)
        assertEquals(1, result.skippedCount)
        assertEquals(1, result.failures.size)
        assertTrue(result.hasFailures)
        assertFalse(result.isSuccess)

        bus.close()
    }

    @Test
    fun `DSL에서 반환된 구독을 개별적으로 해제할 수 있다`() {
        var executed = false
        lateinit var subscription: Subscription

        val bus = eventBus {
            subscription = on<UserCreated> {
                handle {
                    executed = true
                }
            }
        }

        subscription.dispose()

        val result = bus.emit(UserCreated(1))

        assertTrue(subscription.isDisposed)
        assertFalse(executed)
        assertEquals(0, result.executedCount)

        bus.close()
    }

    @Test
    fun `구독을 여러 번 해제해도 안전하다`() {
        var executed = 0
        lateinit var subscription: Subscription

        val bus = eventBus {
            subscription = on<UserCreated> {
                handle {
                    executed++
                }
            }
        }

        subscription.dispose()
        subscription.dispose()
        subscription.close()

        bus.emit(UserCreated(1))

        assertTrue(subscription.isDisposed)
        assertEquals(0, executed)

        bus.close()
    }

    @Test
    fun `구독마다 고유한 식별자를 부여한다`() {
        lateinit var first: Subscription
        lateinit var second: Subscription

        val bus = eventBus {
            first = on<UserCreated> {
                handle {}
            }

            second = on<UserCreated> {
                handle {}
            }
        }

        assertTrue(second.id > first.id)

        bus.close()
    }

    @Test
    fun `이벤트 발행 중 구독을 해제해도 현재 발행은 유지한다`() {
        var firstExecuted = false
        var secondExecuted = false

        lateinit var secondSubscription: Subscription

        val bus = eventBus {
            on<UserCreated> {
                handle {
                    firstExecuted = true
                    secondSubscription.dispose()
                }
            }

            secondSubscription = on<UserCreated> {
                handle {
                    secondExecuted = true
                }
            }
        }

        bus.emit(UserCreated(1))

        assertTrue(firstExecuted)
        assertTrue(secondExecuted)

        secondExecuted = false

        bus.emit(UserCreated(2))

        assertFalse(secondExecuted)

        bus.close()
    }

    @Test
    fun `이벤트 버스를 종료하면 모든 구독이 제거된다`() {
        var executed = false

        val bus = eventBus {
            on<UserCreated> {
                handle {
                    executed = true
                }
            }
        }

        bus.close()

        assertTrue(bus.isClosed)

        assertFailsWith<IllegalStateException> {
            bus.emit(UserCreated(1))
        }

        assertFalse(executed)
    }

    @Test
    fun `이벤트 버스를 여러 번 종료해도 안전하다`() {
        val bus = eventBus {
            on<UserCreated> {
                handle {}
            }
        }

        bus.close()
        bus.close()

        assertTrue(bus.isClosed)
    }

    @Test
    fun `종료된 이벤트 버스에는 구독할 수 없다`() {
        val bus = eventBus {
            on<UserCreated> {
                handle {}
            }
        }

        bus.close()

        assertFailsWith<IllegalStateException> {
            bus.subscribe<UserCreated> {}
        }
    }

    @Test
    fun `use 블록을 벗어나면 이벤트 버스를 종료한다`() {
        lateinit var busReference: EventBus

        eventBus {
            on<UserCreated> {
                handle {}
            }
        }.use { bus ->
            busReference = bus

            assertFalse(bus.isClosed)
        }

        assertTrue(busReference.isClosed)
    }

    @Test
    fun `여러 스레드에서 동시에 이벤트를 발행할 수 있다`() {
        val counter = AtomicInteger(0)

        val bus = eventBus {
            on<UserCreated> {
                handle {
                    counter.incrementAndGet()
                }
            }
        }

        val executor = Executors.newFixedThreadPool(8)

        try {
            val futures = (1..8).map {
                executor.submit {
                    repeat(1_000) { index ->
                        bus.emit(UserCreated(index.toLong()))
                    }
                }
            }

            futures.forEach {
                it.get()
            }

            assertEquals(8_000, counter.get())
        } finally {
            executor.shutdownNow()
            bus.close()
        }
    }

    @Test
    fun `여러 스레드에서 동시에 구독을 등록하고 해제할 수 있다`() {
        val subscriptions = ConcurrentLinkedQueue<Subscription>()
        val bus = eventBus {}
        val executor = Executors.newFixedThreadPool(4)

        try {
            val futures = (1..4).map {
                executor.submit {
                    repeat(1_000) {
                        val subscription = bus.subscribe<UserCreated> {}
                        subscriptions.add(subscription)
                    }
                }
            }

            futures.forEach {
                it.get()
            }

            assertEquals(4_000, subscriptions.size)

            subscriptions.forEach {
                it.dispose()
            }

            assertEquals(
                0,
                bus.emit(UserCreated(1)).executedCount
            )
        } finally {
            executor.shutdownNow()
            bus.close()
        }
    }

    @Test
    fun `DSL에서 핸들러를 지정하지 않으면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            eventBus {
                on<UserCreated> {
                    priority(10)
                }
            }
        }
    }

    @Test
    fun `이벤트 버스를 종료하면 모든 구독이 해제 상태가 된다`() {
        lateinit var first: Subscription
        lateinit var second: Subscription

        val bus = eventBus {
            first = on<UserCreated> {
                handle {}
            }

            second = on<UserDeleted> {
                handle {}
            }
        }

        assertFalse(first.isDisposed)
        assertFalse(second.isDisposed)

        bus.close()

        assertTrue(first.isDisposed)
        assertTrue(second.isDisposed)
        assertTrue(bus.isClosed)
    }

    @Test
    fun `이미 해제된 구독이 있어도 이벤트 버스를 정상적으로 종료한다`() {
        lateinit var first: Subscription
        lateinit var second: Subscription

        val bus = eventBus {
            first = on<UserCreated> {
                handle {}
            }

            second = on<UserDeleted> {
                handle {}
            }
        }

        first.dispose()

        bus.close()

        assertTrue(first.isDisposed)
        assertTrue(second.isDisposed)
        assertTrue(bus.isClosed)
    }
}