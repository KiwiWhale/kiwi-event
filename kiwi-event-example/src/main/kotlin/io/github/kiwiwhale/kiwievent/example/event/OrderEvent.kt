package io.github.kiwiwhale.kiwievent.example.event

data class OrderCreated(
    val orderId: Long,
    val userId: Long,
    val totalPrice: Int
)

data class OrderPaid(
    val orderId: Long,
    val paymentId: String,
    val amount: Int
)

data class OrderCancelled(
    val orderId: Long,
    val reason: String
)