package com.shopsphere.inventory.dto;

import java.time.LocalDateTime;

public record InventoryResponse(

        Long id,
        Long productId,
        Integer availableQuantity,
        Integer reservedQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}