package io.github.kiwiwhale.kiwievent.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SuspendingEventBusTest {

    private data class UserCreated(val userId: Long)

    private data class PaymentCompleted(val orderId: Long)

    @Test
    fun `Suspend Handler에게 이벤트를 전달한다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        var received: UserCreated? = null

        eventBus.subscribe<UserCreated> {
            received = it
        }

        val result = eventBus.publish(UserCreated(100))

        assertEquals(100L, received?.userId)
        assertEquals(1, result.successfulHandlers)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `여러 Suspend Handler를 순서대로 실행한다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        val order = mutableListOf<Int>()

        repeat(3) { index ->
            eventBus.subscribe<UserCreated> {
                order.add(index + 1)
            }
        }

        eventBus.publish(UserCreated(100))

        assertEquals(listOf(1, 2, 3), order)
    }

    @Test
    fun `우선순위가 높은 Suspend Handler를 먼저 실행한다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        val order = mutableListOf<Int>()

        eventBus.subscribe<UserCreated>(priority = 1) {
            order.add(1)
        }

        eventBus.subscribe<UserCreated>(priority = 10) {
            order.add(10)
        }

        eventBus.subscribe<UserCreated>(priority = 5) {
            order.add(5)
        }

        eventBus.publish(UserCreated(100))

        assertEquals(listOf(10, 5, 1), order)
    }

    @Test
    fun `일회성 Suspend 구독은 한 번만 실행한다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        var count = 0

        val subscription = eventBus.subscribe<UserCreated>(once = true) {
            count++
        }

        eventBus.publish(UserCreated(100))
        eventBus.publish(UserCreated(200))

        assertEquals(1, count)
        assertFalse(subscription.isActive)
    }

    @Test
    fun `일회성 Suspend 구독은 재진입 발행에서도 한 번만 실행한다`() =
        runBlocking {
            val eventBus = SuspendingEventBus()
            var count = 0

            eventBus.subscribe<UserCreated>(once = true) {
                count++
                eventBus.publish(UserCreated(200))
            }

            eventBus.publish(UserCreated(100))

            assertEquals(1, count)
        }

    @Test
    fun `Suspend Handler 실패 후에도 다음 Handler를 실행한다`() =
        runBlocking {
            val eventBus = SuspendingEventBus()
            var secondExecuted = false

            eventBus.subscribe<UserCreated> {
                error("Handler failed")
            }

            eventBus.subscribe<UserCreated> {
                secondExecuted = true
            }

            val result = eventBus.publish(UserCreated(100))

            assertTrue(secondExecuted)
            assertEquals(1, result.successfulHandlers)
            assertEquals(1, result.failedHandlers)
        }

    @Test
    fun `Coroutine 취소 예외를 전파한다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        var secondExecuted = false

        eventBus.subscribe<UserCreated> {
            throw CancellationException("Cancelled")
        }

        eventBus.subscribe<UserCreated> {
            secondExecuted = true
        }

        assertFailsWith<CancellationException> {
            eventBus.publish(UserCreated(100))
        }

        assertFalse(secondExecuted)
    }

    @Test
    fun `Suspend 구독을 취소하면 이벤트를 전달하지 않는다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        var received = false

        val subscription = eventBus.subscribe<UserCreated> {
            received = true
        }

        subscription.cancel()
        eventBus.publish(UserCreated(100))

        assertFalse(received)
        assertFalse(subscription.isActive)
    }

    @Test
    fun `특정 이벤트 타입의 Suspend 구독만 제거한다`() = runBlocking {
        val eventBus = SuspendingEventBus()

        val user = eventBus.subscribe<UserCreated> {}
        val payment = eventBus.subscribe<PaymentCompleted> {}

        val removed = eventBus.unsubscribeAll<UserCreated>()

        assertEquals(1, removed)
        assertFalse(user.isActive)
        assertTrue(payment.isActive)
    }

    @Test
    fun `전체 Suspend 구독을 제거한다`() = runBlocking {
        val eventBus = SuspendingEventBus()

        val first = eventBus.subscribe<UserCreated> {}
        val second = eventBus.subscribe<PaymentCompleted> {}

        assertEquals(2, eventBus.clear())

        assertFalse(first.isActive)
        assertFalse(second.isActive)
        assertEquals(0, eventBus.totalSubscriberCount)
    }

    @Test
    fun `구독자가 없는 경우 빈 결과를 반환한다`() = runBlocking {
        val eventBus = SuspendingEventBus()

        val result = eventBus.publish(UserCreated(100))

        assertEquals(0, result.attemptedHandlers)
        assertTrue(result.isSuccess)
        assertFalse(result.wasHandled)
    }

    @Test
    fun `여러 Coroutine에서 일회성 구독을 동시에 발행해도 한 번만 실행한다`() =
        runBlocking {
            val eventBus = SuspendingEventBus()
            var count = 0

            eventBus.subscribe<UserCreated>(once = true) {
                count++
            }

            (1..16).map {
                async {
                    eventBus.publish(UserCreated(it.toLong()))
                }
            }.awaitAll()

            assertEquals(1, count)
        }

    @Test
    fun `Suspend Handler에서 중첩 발행을 수행한다`() = runBlocking {
        val eventBus = SuspendingEventBus()
        val received = mutableListOf<Long>()

        eventBus.subscribe<UserCreated> {
            received.add(it.userId)

            if (it.userId == 100L) {
                eventBus.publish(UserCreated(200))
            }
        }

        eventBus.publish(UserCreated(100))

        assertEquals(listOf(100L, 200L), received)
    }
}