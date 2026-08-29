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
import com.shopsphere.order.entity.OrderStatus;
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

        ProductClient.ProductResponse product =
                productClient.getProduct(request.productId());

        InventoryClient.InventoryResponse inventory =
                inventoryClient.getInventoryByProductId(request.productId());

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new IllegalArgumentException(
                    "Insufficient available inventory for product "
                            + request.productId()
            );
        }

        inventoryClient.reserveInventory(
                inventory.getId(),
                request.quantity()
        );

        try {
            BigDecimal totalAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(request.quantity()));

            Order order = new Order();
            order.setCustomerId(request.customerId());
            order.setProductId(request.productId());
            order.setQuantity(request.quantity());
            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.CREATED.name());
            order.setCreatedAt(now);
            order.setUpdatedAt(now);

            return toResponse(orderRepository.save(order));

        } catch (RuntimeException ex) {
            try {
                inventoryClient.releaseInventory(
                        inventory.getId(),
                        request.quantity()
                );
            } catch (RuntimeException compensationException) {
                ex.addSuppressed(compensationException);
            }

            throw ex;
        }
    }

    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrder(
            Long id,
            OrderRequest request) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        ProductClient.ProductResponse product =
                productClient.getProduct(request.productId());

        Long oldProductId = order.getProductId();
        Integer oldQuantity = order.getQuantity();

        boolean inventoryChanged =
                !oldProductId.equals(request.productId())
                        || !oldQuantity.equals(request.quantity());

        InventoryClient.InventoryResponse oldInventory = null;
        InventoryClient.InventoryResponse newInventory = null;

        if (inventoryChanged) {
            oldInventory = inventoryClient.getInventoryByProductId(oldProductId);
            newInventory = inventoryClient.getInventoryByProductId(request.productId());

            if (newInventory.getAvailableQuantity() < request.quantity()) {
                throw new IllegalArgumentException(
                        "Insufficient available inventory for product "
                                + request.productId()
                );
            }

            inventoryClient.releaseInventory(
                    oldInventory.getId(),
                    oldQuantity
            );

            try {
                inventoryClient.reserveInventory(
                        newInventory.getId(),
                        request.quantity()
                );
            } catch (RuntimeException ex) {
                try {
                    inventoryClient.reserveInventory(
                            oldInventory.getId(),
                            oldQuantity
                    );
                } catch (RuntimeException compensationException) {
                    ex.addSuppressed(compensationException);
                }
                throw ex;
            }
        }

        try {
            BigDecimal totalAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(request.quantity()));

            order.setCustomerId(request.customerId());
            order.setProductId(request.productId());
            order.setQuantity(request.quantity());
            order.setTotalAmount(totalAmount);
            order.setUpdatedAt(LocalDateTime.now());

            return toResponse(orderRepository.save(order));

        } catch (RuntimeException ex) {
            if (inventoryChanged && newInventory != null && oldInventory != null) {
                try {
                    inventoryClient.releaseInventory(
                            newInventory.getId(),
                            request.quantity()
                    );
                    inventoryClient.reserveInventory(
                            oldInventory.getId(),
                            oldQuantity
                    );
                } catch (RuntimeException compensationException) {
                    ex.addSuppressed(compensationException);
                }
            }

            throw ex;
        }
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
