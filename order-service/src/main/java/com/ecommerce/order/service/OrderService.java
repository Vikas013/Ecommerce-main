package com.ecommerce.order.service;

import com.ecommerce.order.config.WebClientConfig;
import com.ecommerce.order.dto.request.OrderLineItemsDto;
import com.ecommerce.order.dto.request.OrderRequestDto;
import com.ecommerce.order.dto.response.InventoryResponseDto;
import com.ecommerce.order.event.OrderPlacedEvent;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderLineItems;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequestDto orderRequestDto){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItemsList = orderRequestDto
                .getOrderLineItemsDtoList().stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItems(orderLineItemsList);

        List<String> skuCodes = order.getOrderLineItems().stream().map(OrderLineItems::getSkuCode)
                .toList();
        //Call Inventory service and check if order is in stock
        InventoryResponseDto[] inventoryResponseArray = webClientBuilder.build().get().uri("http://inventory-service:8082/api/inventory",
                uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve().bodyToMono(InventoryResponseDto[].class)
                .block();

        boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponseDto::isInStock);
        if(allProductsInStock) {
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic",new OrderPlacedEvent(order.getOrderNumber()));
            return "Order Placed Successfully";
        }
        else throw new IllegalArgumentException("Product is not in stock");
    }

    public OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        return OrderLineItems.builder()
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .skuCode(orderLineItemsDto.getSkuCode())
                .build();
    }
}
