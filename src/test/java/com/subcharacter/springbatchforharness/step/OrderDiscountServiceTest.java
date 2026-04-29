package com.subcharacter.springbatchforharness.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.subcharacter.springbatchforharness.domain.Order;
import com.subcharacter.springbatchforharness.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderDiscountServiceTest {

  private OrderDiscountService service;

  @BeforeEach
  void setUp() {
    service = new OrderDiscountService();
  }

  @Test
  void 수량과_금액_모두_기준_미달이면_할인없음() {
    Order order =
        Order.builder()
            .orderId("O001")
            .quantity(1)
            .unitPrice(100.0)
            .status(OrderStatus.PENDING)
            .build();
    assertThat(service.calculateDiscountRate(order)).isEqualTo(0.0);
  }

  @Test
  void 수량_기준_충족시_대량할인_10퍼센트() {
    // quantity=5, totalPrice=500 → bulk only
    Order order =
        Order.builder()
            .orderId("O001")
            .quantity(5)
            .unitPrice(100.0)
            .status(OrderStatus.PENDING)
            .build();
    assertThat(service.calculateDiscountRate(order)).isCloseTo(0.10, within(0.001));
  }

  @Test
  void 금액_기준_충족시_고가할인_15퍼센트() {
    // quantity=1, totalPrice=1000 → high value only
    Order order =
        Order.builder()
            .orderId("O001")
            .quantity(1)
            .unitPrice(1000.0)
            .status(OrderStatus.PENDING)
            .build();
    assertThat(service.calculateDiscountRate(order)).isCloseTo(0.15, within(0.001));
  }

  @Test
  void 두_조건_모두_충족시_최대할인율_20퍼센트로_제한() {
    // quantity=10, totalPrice=2000 → bulk(10%) + high value(15%) = 25% → capped at 20%
    Order order =
        Order.builder()
            .orderId("O001")
            .quantity(10)
            .unitPrice(200.0)
            .status(OrderStatus.PENDING)
            .build();
    assertThat(service.calculateDiscountRate(order)).isCloseTo(0.20, within(0.001));
  }

  @Test
  void 할인금액_계산() {
    // quantity=5, unitPrice=100 → totalPrice=500, rate=10% → discount=50
    Order order =
        Order.builder()
            .orderId("O001")
            .quantity(5)
            .unitPrice(100.0)
            .status(OrderStatus.PENDING)
            .build();
    assertThat(service.calculateDiscountAmount(order)).isCloseTo(50.0, within(0.001));
  }

  @Test
  void 할인_적용후_상태가_PROCESSING으로_변경() {
    Order order =
        Order.builder()
            .orderId("O001")
            .productName("노트북")
            .quantity(1)
            .unitPrice(100.0)
            .status(OrderStatus.PENDING)
            .build();
    Order result = service.applyDiscount(order);
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
  }

  @Test
  void 할인_적용후_주문ID_유지() {
    Order order =
        Order.builder()
            .orderId("O001")
            .productName("노트북")
            .quantity(1)
            .unitPrice(200.0)
            .status(OrderStatus.PENDING)
            .build();
    Order result = service.applyDiscount(order);
    assertThat(result.getOrderId()).isEqualTo("O001");
  }

  @Test
  void 할인_미적용시_최종금액이_원가와_동일() {
    // no discount → finalTotal == totalPrice
    Order order =
        Order.builder()
            .orderId("O001")
            .quantity(1)
            .unitPrice(100.0)
            .status(OrderStatus.PENDING)
            .build();
    Order result = service.applyDiscount(order);
    assertThat(result.getTotalPrice()).isCloseTo(order.getTotalPrice(), within(0.001));
  }
}
