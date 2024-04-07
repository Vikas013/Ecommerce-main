package com.ecommerce.product.Controller;

import com.ecommerce.product.Model.Product;
import com.ecommerce.product.Repository.ProductRepository;
import com.ecommerce.product.dto.Request.ProductRequestDto;
import com.ecommerce.product.dto.Response.ProductResponseDto;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    @Autowired
    private final ProductService productService;
    @Autowired
    private final ProductRepository productRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addProduct(@RequestBody ProductRequestDto productRequestDto){
        productService.addProduct(productRequestDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponseDto> getAllProducts(){
        return productRepository.findAll()
                .stream().map(this::mapToProductResponse).collect(Collectors.toList());
    }

    public ProductResponseDto mapToProductResponse(Product product){
        return ProductResponseDto.builder()
                .id(product.getId())
                .description(product.getDescription())
                .name(product.getName())
                .price(product.getPrice()).build();
    }


}
