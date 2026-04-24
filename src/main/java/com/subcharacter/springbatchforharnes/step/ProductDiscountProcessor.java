package com.subcharacter.springbatchforharnes.step;

import com.subcharacter.springbatchforharnes.domain.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductDiscountProcessor implements ItemProcessor<Product, Product> {

    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final double DISCOUNT_RATE = 0.10;

    @Override
    public Product process(Product item) {
        if (item.getPrice() > DISCOUNT_THRESHOLD) {
            double discountedPrice = item.getPrice() * (1 - DISCOUNT_RATE);
            log.info("할인 적용: {} {}원 → {}원", item.getName(), item.getPrice(), discountedPrice);
            return Product.builder()
                    .name(item.getName())
                    .price(discountedPrice)
                    .build();
        }
        log.info("할인 미적용: {} {}원", item.getName(), item.getPrice());
        return item;
    }
}
