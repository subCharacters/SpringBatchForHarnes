package com.subcharacter.springbatchforharnes.step;

import com.subcharacter.springbatchforharnes.domain.Order;
import com.subcharacter.springbatchforharnes.domain.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 주문 유효성 검사기. */
@Slf4j
@Component
public class OrderValidator {

  /** 주문이 처리 가능한 상태인지 검사한다. */
  public boolean isValid(Order order) {
    if (order == null) {
      log.warn("주문이 null입니다.");
      return false;
    }
    if (order.getOrderId() == null || order.getOrderId().isBlank()) {
      log.warn("주문 ID가 없습니다.");
      return false;
    }
    if (order.getQuantity() <= 0) {
      log.warn("주문 수량이 0 이하입니다. orderId={}", order.getOrderId());
      return false;
    }
    if (order.getUnitPrice() <= 0) {
      log.warn("단가가 0 이하입니다. orderId={}", order.getOrderId());
      return false;
    }
    if (order.getStatus() == OrderStatus.CANCELLED) {
      log.warn("취소된 주문입니다. orderId={}", order.getOrderId());
      return false;
    }
    if (order.getStatus() == OrderStatus.COMPLETED) {
      log.warn("이미 완료된 주문입니다. orderId={}", order.getOrderId());
      return false;
    }
    return true;
  }
}
