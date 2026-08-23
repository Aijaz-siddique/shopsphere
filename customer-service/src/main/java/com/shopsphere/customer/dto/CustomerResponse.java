package com.shopsphere.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponse(

        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}