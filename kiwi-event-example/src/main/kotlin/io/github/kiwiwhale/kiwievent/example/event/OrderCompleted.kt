package io.github.kiwiwhale.kiwievent.example.event

data class OrderCompleted(
    val orderId: Long,
    val userId: Long,
    val totalPrice: Long
)