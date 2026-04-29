package com.subcharacter.springbatchforharness.job;

import com.subcharacter.springbatchforharness.step.OrderProcessingTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/** 주문 처리 배치 Job 설정. */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderJobConfig {

  private final OrderProcessingTasklet orderProcessingTasklet;

  /** 주문 처리 Job 빈. */
  @Bean
  public Job orderJob(JobRepository jobRepository, Step orderStep) {
    return new JobBuilder("orderJob", jobRepository).start(orderStep).build();
  }

  /** 주문 처리 Step 빈. */
  @Bean
  public Step orderStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("orderStep", jobRepository)
        .tasklet(orderProcessingTasklet, transactionManager)
        .build();
  }
}
