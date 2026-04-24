package com.subcharacter.springbatchforharnes.step;

import com.subcharacter.springbatchforharnes.domain.Order;
import com.subcharacter.springbatchforharnes.domain.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderDiscountService {

    private static final int BULK_QUANTITY_THRESHOLD = 5;
    private static final double BULK_DISCOUNT_RATE = 0.10;
    private static final double HIGH_VALUE_THRESHOLD = 1000.0;
    private static final double HIGH_VALUE_DISCOUNT_RATE = 0.15;
    private static final double MAX_DISCOUNT_RATE = 0.20;

    public double calculateDiscountRate(Order order) {
        double rate = 0.0;

        if (order.getQuantity() >= BULK_QUANTITY_THRESHOLD) {
            rate += BULK_DISCOUNT_RATE;
            log.debug("대량 할인 적용: orderId={}, +{}%", order.getOrderId(), (int)(BULK_DISCOUNT_RATE * 100));
        }
        if (order.getTotalPrice() >= HIGH_VALUE_THRESHOLD) {
            rate += HIGH_VALUE_DISCOUNT_RATE;
            log.debug("고가 할인 적용: orderId={}, +{}%", order.getOrderId(), (int)(HIGH_VALUE_DISCOUNT_RATE * 100));
        }
        if (rate > MAX_DISCOUNT_RATE) {
            rate = MAX_DISCOUNT_RATE;
        }
        return rate;
    }

    public double calculateDiscountAmount(Order order) {
        return order.getTotalPrice() * calculateDiscountRate(order);
    }

    public Order applyDiscount(Order order) {
        double discountAmount = calculateDiscountAmount(order);
        double finalTotal = order.getTotalPrice() - discountAmount;

        log.info("할인 처리: orderId={}, 원가={}원, 할인={}원, 최종={}원",
                order.getOrderId(), order.getTotalPrice(), discountAmount, finalTotal);

        return Order.builder()
                .orderId(order.getOrderId())
                .productName(order.getProductName())
                .quantity(order.getQuantity())
                .unitPrice(finalTotal / order.getQuantity())
                .status(OrderStatus.PROCESSING)
                .build();
    }
}
