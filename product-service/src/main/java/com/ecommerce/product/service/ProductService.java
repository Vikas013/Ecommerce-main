package com.ecommerce.product.service;

import com.ecommerce.product.Model.Product;
import com.ecommerce.product.Repository.ProductRepository;
import com.ecommerce.product.dto.Request.ProductRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    @Autowired
    private final ProductRepository productRepository;

    public void addProduct(ProductRequestDto productRequestDto){
        Product product = Product.builder()
                .description(productRequestDto.getDescription())
                .name(productRequestDto.getName())
                .price(productRequestDto.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product {} is saved",product.getId());
    }
}
