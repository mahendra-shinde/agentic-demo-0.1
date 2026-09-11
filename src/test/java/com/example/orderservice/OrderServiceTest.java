package com.example.orderservice;

import com.example.orderservice.dto.OrderItemRequest;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private final OrderService orderService = new OrderServiceImpl();

    @Test
    void shouldApproveOrderWithoutDiscountWhenTotalAtOrBelowThreshold() {
        OrderRequest request = new OrderRequest(
                "cust-1",
                List.of(new OrderItemRequest("SKU-1", 2, new BigDecimal("100.00")))
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.APPROVED);
        assertThat(response.rawTotal()).isEqualByComparingTo("200.00");
        assertThat(response.discountedTotal()).isEqualByComparingTo("200.00");
        assertThat(response.rejectionReason()).isNull();
    }

    @Test
    void shouldApplyDiscountWhenRawTotalExceedsThreshold() {
        OrderRequest request = new OrderRequest(
                "cust-2",
                List.of(new OrderItemRequest("SKU-2", 3, new BigDecimal("200.00")))
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.APPROVED);
        assertThat(response.rawTotal()).isEqualByComparingTo("600.00");
        assertThat(response.discountedTotal()).isEqualByComparingTo("540.00");
    }

    @Test
    void shouldRejectOrderWithDiscontinuedSku() {
        OrderRequest request = new OrderRequest(
                "cust-3",
                List.of(new OrderItemRequest("DISCONTINUED-ABC", 1, new BigDecimal("1000.00")))
        );

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Order contains discontinued items");
        assertThat(response.rawTotal()).isEqualByComparingTo("1000.00");
        assertThat(response.discountedTotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    void shouldPersistOrderForLookupById() {
        OrderRequest request = new OrderRequest(
                "cust-4",
                List.of(new OrderItemRequest("SKU-4", 1, new BigDecimal("50.00")))
        );

        OrderResponse created = orderService.createOrder(request);

        assertThat(orderService.getOrderById(created.id())).contains(created);
    }
}
