package com.ecommerce.order.controller;

import com.ecommerce.order.dto.request.OrderRequestDto;
import com.ecommerce.order.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/order")
public class OrderController {
    @Autowired
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallBack")
    @TimeLimiter(name = "inventory",fallbackMethod = "fallBack")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequestDto orderRequestDto){
        return CompletableFuture.supplyAsync(()->orderService.placeOrder(orderRequestDto));

    }
    public CompletableFuture<String> fallBack(OrderRequestDto orderRequestDto,RuntimeException exception){
        return CompletableFuture.supplyAsync(()->"Oops! Its not you its us, Please retry after some time");
    }
}
