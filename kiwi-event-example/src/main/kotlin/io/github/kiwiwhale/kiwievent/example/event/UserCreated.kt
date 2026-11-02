package io.github.kiwiwhale.kiwievent.example.event

data class UserCreated(
    val userId: Long,
    val username: String
)