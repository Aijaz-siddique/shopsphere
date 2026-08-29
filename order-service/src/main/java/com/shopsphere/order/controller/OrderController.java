package com.shopsphere.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopsphere.order.dto.OrderRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id) {

        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {

        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(
            @PathVariable Long id) {

        return ResponseEntity.ok(orderService.confirmOrder(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {

        orderService.deleteOrder(id);
    }
}
