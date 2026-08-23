package com.shopsphere.order.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
        BigDecimal totalAmount

) {
}