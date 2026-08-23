package com.shopsphere.inventory.dto;

public record InventoryAvailabilityResponse(

        Long productId,
        Integer availableQuantity,
        Integer reservedQuantity,
        Integer totalQuantity,
        boolean available

) {
}