package com.subcharacter.springbatchforharnes.job;

import com.subcharacter.springbatchforharnes.domain.Product;
import com.subcharacter.springbatchforharnes.step.ProductDiscountProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductJobConfig {

    private final ProductDiscountProcessor productDiscountProcessor;

    @Bean
    public Job productJob(JobRepository jobRepository, Step productStep) {
        return new JobBuilder("productJob", jobRepository)
                .start(productStep)
                .build();
    }

    @Bean
    public Step productStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("productStep", jobRepository)
                .<Product, Product>chunk(5, transactionManager)
                .reader(productItemReader())
                .processor(productDiscountProcessor)
                .writer(productItemWriter())
                .build();
    }

    @Bean
    public ListItemReader<Product> productItemReader() {
        return new ListItemReader<>(List.of(
                new Product("노트북", 999.99),
                new Product("마우스", 29.99),
                new Product("키보드", 149.99),
                new Product("모니터", 299.99),
                new Product("USB 허브", 19.99)
        ));
    }

    @Bean
    public ItemWriter<Product> productItemWriter() {
        return chunk -> chunk.getItems().forEach(product ->
                log.info("저장 완료: {} | 최종 가격: {}원", product.getName(), product.getPrice()));
    }
}
