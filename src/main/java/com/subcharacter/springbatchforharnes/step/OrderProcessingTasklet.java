package com.subcharacter.springbatchforharnes.step;

import com.subcharacter.springbatchforharnes.domain.Order;
import com.subcharacter.springbatchforharnes.domain.OrderStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/** 주문 유효성 검사 및 할인 처리를 수행하는 Tasklet. */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProcessingTasklet implements Tasklet {

  private final OrderValidator orderValidator;
  private final OrderDiscountService orderDiscountService;

  private static final List<Order> SAMPLE_ORDERS =
      List.of(
          Order.builder()
              .orderId("O001")
              .productName("노트북")
              .quantity(1)
              .unitPrice(999.99)
              .status(OrderStatus.PENDING)
              .build(),
          Order.builder()
              .orderId("O002")
              .productName("마우스")
              .quantity(10)
              .unitPrice(29.99)
              .status(OrderStatus.PENDING)
              .build(),
          Order.builder()
              .orderId("O003")
              .productName("키보드")
              .quantity(3)
              .unitPrice(149.99)
              .status(OrderStatus.CANCELLED)
              .build(),
          Order.builder()
              .orderId("O004")
              .productName("모니터")
              .quantity(5)
              .unitPrice(299.99)
              .status(OrderStatus.PENDING)
              .build(),
          Order.builder()
              .orderId("O005")
              .productName("USB 허브")
              .quantity(0)
              .unitPrice(19.99)
              .status(OrderStatus.PENDING)
              .build());

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    log.info("주문 처리 시작: 총 {}건", SAMPLE_ORDERS.size());

    int processed = 0;
    int skipped = 0;

    for (Order order : SAMPLE_ORDERS) {
      if (!orderValidator.isValid(order)) {
        log.warn("건너뜀: orderId={}", order.getOrderId());
        skipped++;
        continue;
      }

      Order result = orderDiscountService.applyDiscount(order);
      log.info("완료: orderId={}, 최종금액={}원", result.getOrderId(), result.getTotalPrice());
      processed++;
    }

    log.info("주문 처리 완료 - 처리: {}건, 건너뜀: {}건", processed, skipped);
    contribution.incrementWriteCount(processed);
    return RepeatStatus.FINISHED;
  }
}
