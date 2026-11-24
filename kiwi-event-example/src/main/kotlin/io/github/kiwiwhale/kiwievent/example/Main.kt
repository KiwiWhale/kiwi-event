package io.github.kiwiwhale.kiwievent.example

import io.github.kiwiwhale.kiwievent.example.event.createEventBus
import io.github.kiwiwhale.kiwievent.example.server.OrderService

fun main() {
    val eventBus = createEventBus()
    val orderService = OrderService(eventBus)

    eventBus.use {
        orderService.createOrder(
            orderId = 1001,
            userId = 10,
            totalPrice = 150_000
        )

        println()

        orderService.payOrder(
            orderId = 1001,
            paymentId = "PAY-001",
            amount = 150_000
        )

        println()

        orderService.cancelOrder(
            orderId = 1001,
            reason = "고객 요청"
        )
    }
}