package com.shopsphere.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(

        Long id,
        Long customerId,
        Long productId,
        Integer quantity,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}