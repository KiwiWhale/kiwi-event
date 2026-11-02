package io.github.kiwiwhale.kiwievent.core

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventBusTest {

    private data class UserCreated(val userId: Long)

    private data class PaymentCompleted(val orderId: Long)

    @Test
    fun `이벤트를 구독자에게 전달한다`() {
        val eventBus = EventBus()
        var received: UserCreated? = null

        eventBus.subscribe<UserCreated> {
            received = it
        }

        val result = eventBus.publish(UserCreated(100))

        assertEquals(100L, received?.userId)
        assertEquals(1, result.attemptedHandlers)
        assertEquals(1, result.successfulHandlers)
        assertTrue(result.isSuccess)
        assertTrue(result.wasHandled)
    }

    @Test
    fun `하나의 이벤트를 여러 구독자에게 전달한다`() {
        val eventBus = EventBus()
        val received = mutableListOf<Long>()

        eventBus.subscribe<UserCreated> {
            received.add(it.userId)
        }

        eventBus.subscribe<UserCreated> {
            received.add(it.userId)
        }

        val result = eventBus.publish(UserCreated(100))

        assertEquals(listOf(100L, 100L), received)
        assertEquals(2, result.successfulHandlers)
    }

    @Test
    fun `구독 등록 순서대로 실행한다`() {
        val eventBus = EventBus()
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
    fun `우선순위가 높은 Handler를 먼저 실행한다`() {
        val eventBus = EventBus()
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
    fun `우선순위가 같으면 등록 순서를 유지한다`() {
        val eventBus = EventBus()
        val order = mutableListOf<Int>()

        repeat(3) { index ->
            eventBus.subscribe<UserCreated>(priority = 5) {
                order.add(index + 1)
            }
        }

        eventBus.publish(UserCreated(100))

        assertEquals(listOf(1, 2, 3), order)
    }

    @Test
    fun `일회성 구독은 한 번만 실행한다`() {
        val eventBus = EventBus()
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
    fun `일회성 구독은 재진입 발행에서도 한 번만 실행한다`() {
        val eventBus = EventBus()
        var count = 0

        eventBus.subscribe<UserCreated>(once = true) {
            count++
            eventBus.publish(UserCreated(200))
        }

        eventBus.publish(UserCreated(100))

        assertEquals(1, count)
    }

    @Test
    fun `구독을 취소하면 이벤트를 전달하지 않는다`() {
        val eventBus = EventBus()
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
    fun `구독을 여러 번 취소해도 안전하다`() {
        val eventBus = EventBus()

        val subscription = eventBus.subscribe<UserCreated> {}

        subscription.cancel()
        subscription.cancel()

        assertFalse(subscription.isActive)
        assertEquals(0, eventBus.totalSubscriberCount)
    }

    @Test
    fun `Handler 실패 후에도 다음 Handler를 실행한다`() {
        val eventBus = EventBus()
        var secondExecuted = false

        eventBus.subscribe<UserCreated> {
            error("Handler failed")
        }

        eventBus.subscribe<UserCreated> {
            secondExecuted = true
        }

        val result = eventBus.publish(UserCreated(100))

        assertTrue(secondExecuted)
        assertEquals(2, result.attemptedHandlers)
        assertEquals(1, result.successfulHandlers)
        assertEquals(1, result.failedHandlers)
        assertTrue(result.hasFailures)
    }

    @Test
    fun `실패한 Handler의 예외 정보를 반환한다`() {
        val eventBus = EventBus()

        eventBus.subscribe<UserCreated> {
            error("처리 실패")
        }

        val result = eventBus.publish(UserCreated(100))

        assertEquals(1, result.failures.size)
        assertEquals("처리 실패", result.failures.first().cause.message)
    }

    @Test
    fun `서로 다른 이벤트 타입의 구독자를 구분한다`() {
        val eventBus = EventBus()

        var userExecuted = false
        var paymentExecuted = false

        eventBus.subscribe<UserCreated> {
            userExecuted = true
        }

        eventBus.subscribe<PaymentCompleted> {
            paymentExecuted = true
        }

        eventBus.publish(UserCreated(100))

        assertTrue(userExecuted)
        assertFalse(paymentExecuted)
    }

    @Test
    fun `구독자가 없으면 성공한 빈 결과를 반환한다`() {
        val eventBus = EventBus()

        val result = eventBus.publish(UserCreated(100))

        assertEquals(0, result.attemptedHandlers)
        assertEquals(0, result.dispatchedHandlers)
        assertEquals(0, result.skippedHandlers)
        assertTrue(result.isSuccess)
        assertFalse(result.wasHandled)
    }

    @Test
    fun `구독자 수를 조회한다`() {
        val eventBus = EventBus()

        eventBus.subscribe<UserCreated> {}
        eventBus.subscribe<UserCreated> {}

        assertEquals(2, eventBus.subscriberCount<UserCreated>())
        assertEquals(2, eventBus.totalSubscriberCount)
    }

    @Test
    fun `특정 이벤트 타입의 구독만 제거한다`() {
        val eventBus = EventBus()

        val user = eventBus.subscribe<UserCreated> {}
        val payment = eventBus.subscribe<PaymentCompleted> {}

        val removed = eventBus.unsubscribeAll<UserCreated>()

        assertEquals(1, removed)
        assertFalse(user.isActive)
        assertTrue(payment.isActive)
        assertEquals(1, eventBus.totalSubscriberCount)
    }

    @Test
    fun `전체 구독을 제거한다`() {
        val eventBus = EventBus()

        val first = eventBus.subscribe<UserCreated> {}
        val second = eventBus.subscribe<PaymentCompleted> {}

        val removed = eventBus.clear()

        assertEquals(2, removed)
        assertFalse(first.isActive)
        assertFalse(second.isActive)
        assertEquals(0, eventBus.totalSubscriberCount)
    }

    @Test
    fun `이벤트 발행 중 추가된 구독자는 현재 발행에 참여하지 않는다`() {
        val eventBus = EventBus()
        var lateExecuted = false

        eventBus.subscribe<UserCreated> {
            eventBus.subscribe<UserCreated> {
                lateExecuted = true
            }
        }

        eventBus.publish(UserCreated(100))

        assertFalse(lateExecuted)

        eventBus.publish(UserCreated(200))

        assertTrue(lateExecuted)
    }

    @Test
    fun `발행 중 취소된 다음 Handler는 실행하지 않는다`() {
        val eventBus = EventBus()
        var secondExecuted = false

        lateinit var second: Subscription

        eventBus.subscribe<UserCreated> {
            second.cancel()
        }

        second = eventBus.subscribe<UserCreated> {
            secondExecuted = true
        }

        eventBus.publish(UserCreated(100))

        assertFalse(secondExecuted)
    }

    @Test
    fun `여러 스레드에서 일회성 구독을 동시에 발행해도 한 번만 실행한다`() {
        val eventBus = EventBus()
        val executionCount = AtomicInteger()

        val threadCount = 16
        val ready = CountDownLatch(threadCount)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)

        eventBus.subscribe<UserCreated>(once = true) {
            executionCount.incrementAndGet()
        }

        try {
            val tasks = (1..threadCount).map {
                executor.submit {
                    ready.countDown()
                    start.await()
                    eventBus.publish(UserCreated(it.toLong()))
                }
            }

            assertTrue(ready.await(5, TimeUnit.SECONDS))
            start.countDown()

            tasks.forEach {
                it.get(5, TimeUnit.SECONDS)
            }

            assertEquals(1, executionCount.get())
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `EventBus 별로 구독을 독립적으로 관리한다`() {
        val firstBus = EventBus()
        val secondBus = EventBus()

        var firstExecuted = false
        var secondExecuted = false

        firstBus.subscribe<UserCreated> {
            firstExecuted = true
        }

        secondBus.subscribe<UserCreated> {
            secondExecuted = true
        }

        firstBus.clear()
        secondBus.publish(UserCreated(100))

        assertFalse(firstExecuted)
        assertTrue(secondExecuted)
    }
}