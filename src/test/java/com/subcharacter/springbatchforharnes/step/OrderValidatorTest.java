package com.subcharacter.springbatchforharnes.step;

import com.subcharacter.springbatchforharnes.domain.Order;
import com.subcharacter.springbatchforharnes.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderValidatorTest {

    private OrderValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrderValidator();
    }

    @Test
    void null_주문은_무효() {
        assertThat(validator.isValid(null)).isFalse();
    }

    @Test
    void 주문ID가_없으면_무효() {
        Order order = Order.builder().orderId("").quantity(1).unitPrice(100.0).status(OrderStatus.PENDING).build();
        assertThat(validator.isValid(order)).isFalse();
    }

    @Test
    void 수량이_0이하면_무효() {
        Order order = Order.builder().orderId("O001").quantity(0).unitPrice(100.0).status(OrderStatus.PENDING).build();
        assertThat(validator.isValid(order)).isFalse();
    }

    @Test
    void 단가가_0이하면_무효() {
        Order order = Order.builder().orderId("O001").quantity(1).unitPrice(0.0).status(OrderStatus.PENDING).build();
        assertThat(validator.isValid(order)).isFalse();
    }

    @Test
    void 취소된_주문은_무효() {
        Order order = Order.builder().orderId("O001").quantity(1).unitPrice(100.0).status(OrderStatus.CANCELLED).build();
        assertThat(validator.isValid(order)).isFalse();
    }

    @Test
    void 완료된_주문은_무효() {
        Order order = Order.builder().orderId("O001").quantity(1).unitPrice(100.0).status(OrderStatus.COMPLETED).build();
        assertThat(validator.isValid(order)).isFalse();
    }

    @Test
    void 유효한_PENDING_주문은_통과() {
        Order order = Order.builder().orderId("O001").productName("노트북").quantity(1).unitPrice(100.0).status(OrderStatus.PENDING).build();
        assertThat(validator.isValid(order)).isTrue();
    }

    @Test
    void 유효한_PROCESSING_주문은_통과() {
        Order order = Order.builder().orderId("O001").productName("마우스").quantity(2).unitPrice(50.0).status(OrderStatus.PROCESSING).build();
        assertThat(validator.isValid(order)).isTrue();
    }
}
