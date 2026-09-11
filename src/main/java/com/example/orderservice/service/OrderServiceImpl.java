package com.example.orderservice.service;

import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String DISCONTINUED_PREFIX = "DISCONTINUED-";
    private static final String REJECTION_REASON = "Order contains discontinued items";
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_MULTIPLIER = new BigDecimal("0.90");

    private final Map<UUID, OrderResponse> orders = new ConcurrentHashMap<>();

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        BigDecimal rawTotal = orderRequest.items().stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean hasDiscontinued = orderRequest.items().stream()
                .map(OrderItemRequest::sku)
                .anyMatch(sku -> sku != null && sku.startsWith(DISCONTINUED_PREFIX));

        OrderStatus status = hasDiscontinued ? OrderStatus.REJECTED : OrderStatus.APPROVED;
        String rejectionReason = hasDiscontinued ? REJECTION_REASON : null;

        BigDecimal discountedTotal = rawTotal;
        if (!hasDiscontinued && rawTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discountedTotal = rawTotal.multiply(DISCOUNT_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
        }

        OrderResponse response = new OrderResponse(
                UUID.randomUUID(),
                orderRequest.customerId(),
                orderRequest.items(),
                rawTotal,
                discountedTotal,
                status,
                rejectionReason
        );

        orders.put(response.id(), response);
        return response;
    }

    @Override
    public Optional<OrderResponse> getOrderById(UUID id) {
        return Optional.ofNullable(orders.get(id));
    }

    private BigDecimal lineTotal(OrderItemRequest item) {
        return item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
    }
}
