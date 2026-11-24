package io.github.kiwiwhale.kiwievent.example.server

import io.github.kiwiwhale.kiwievent.core.EventBus
import io.github.kiwiwhale.kiwievent.example.event.*

class OrderService(
    private val eventBus: EventBus
) {

    fun createOrder(
        orderId: Long,
        userId: Long,
        totalPrice: Int
    ) {
        println("주문 생성 처리")

        eventBus.emit(
            OrderCreated(
                orderId = orderId,
                userId = userId,
                totalPrice = totalPrice
            )
        )
    }

    fun payOrder(
        orderId: Long,
        paymentId: String,
        amount: Int
    ) {
        println("결제 처리")

        eventBus.emit(
            OrderPaid(
                orderId = orderId,
                paymentId = paymentId,
                amount = amount
            )
        )
    }

    fun cancelOrder(
        orderId: Long,
        reason: String
    ) {
        println("주문 취소 처리")

        eventBus.emit(
            OrderCancelled(
                orderId = orderId,
                reason = reason
            )
        )
    }
}