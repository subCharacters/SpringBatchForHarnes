package com.subcharacter.springbatchforharnes.step;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OrderProcessingTaskletTest {

    private OrderProcessingTasklet tasklet;

    @BeforeEach
    void setUp() {
        OrderValidator validator = new OrderValidator();
        OrderDiscountService discountService = new OrderDiscountService();
        tasklet = new OrderProcessingTasklet(validator, discountService);
    }

    @Test
    void execute가_FINISHED를_반환한다() throws Exception {
        StepContribution contribution = mock(StepContribution.class);
        ChunkContext chunkContext = mock(ChunkContext.class);

        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }
}
