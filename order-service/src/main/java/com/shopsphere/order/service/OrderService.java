package com.shopsphere.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.order.client.InventoryClient;
import com.shopsphere.order.client.ProductClient;
import com.shopsphere.order.dto.OrderRequest;
import com.shopsphere.order.dto.OrderResponse;
import com.shopsphere.order.entity.Order;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.order.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderService(
            OrderRepository orderRepository,
            ProductClient productClient,
            InventoryClient inventoryClient) {

        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // 1. Verify product exists and get its price
        ProductClient.ProductResponse product =
                productClient.getProduct(request.productId());

        // 2. Get inventory for the requested product
        InventoryClient.InventoryResponse inventory =
                inventoryClient.getInventoryByProductId(
                        request.productId()
                );

        // 3. Check inventory before attempting reservation
        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new IllegalArgumentException(
                    "Insufficient available inventory for product "
                            + request.productId()
            );
        }

        // 4. Reserve inventory
        inventoryClient.reserveInventory(
                inventory.getId(),
                request.quantity()
        );

        // 5. Calculate total order amount
        BigDecimal totalAmount =
                product.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.quantity()
                                )
                        );

        // 6. Create order
        Order order = new Order();

        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // 7. Persist order
        Order savedOrder =
                orderRepository.save(order);

        return toResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

        return toResponse(order);
    }

    public OrderResponse updateOrder(
            Long id,
            OrderRequest request) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id)
                );

        // Verify product exists and get current price
        ProductClient.ProductResponse product =
                productClient.getProduct(request.productId());

        BigDecimal totalAmount =
                product.getPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.quantity()
                                )
                        );

        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalAmount(totalAmount);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder =
                orderRepository.save(order);

        return toResponse(updatedOrder);
    }

    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }

        orderRepository.deleteById(id);
    }

    private OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}