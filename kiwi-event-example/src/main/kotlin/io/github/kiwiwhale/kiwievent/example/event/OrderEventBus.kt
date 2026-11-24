package io.github.kiwiwhale.kiwievent.example.event

import io.github.kiwiwhale.kiwievent.core.dsl.eventBus
import io.github.kiwiwhale.kiwievent.core.dsl.on

fun createEventBus() = eventBus {

    on<OrderCreated> {
        priority(100)

        handle { event ->
            println("[ORDER] 주문 생성: ${event.orderId}")
        }
    }

    on<OrderCreated> {
        priority(50)

        filter { event ->
            event.totalPrice >= 100_000
        }

        handle { event ->
            println("[VIP] 고액 주문 감지: ${event.orderId}")
        }
    }

    on<OrderCreated> {
        priority(0)

        handle { event ->
            println("[EMAIL] 주문 확인 이메일 발송: ${event.userId}")
        }
    }

    on<OrderPaid> {
        priority(100)

        handle { event ->
            println("[PAYMENT] 결제 완료: ${event.paymentId}")
        }
    }

    on<OrderPaid> {
        priority(50)

        handle { event ->
            println("[POINT] 포인트 적립: ${event.amount / 100}")
        }
    }

    on<OrderCancelled> {
        handle { event ->
            println("[ORDER] 주문 취소: ${event.orderId}")
            println("[REASON] ${event.reason}")
        }
    }
}