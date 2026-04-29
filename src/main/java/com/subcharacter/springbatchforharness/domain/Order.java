package com.subcharacter.springbatchforharness.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 주문 도메인 모델. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
  private String orderId;
  private String productName;
  private int quantity;
  private double unitPrice;
  private OrderStatus status;

  public double getTotalPrice() {
    return quantity * unitPrice;
  }
}
