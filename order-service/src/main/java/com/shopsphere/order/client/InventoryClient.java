package com.shopsphere.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${inventory-service.url}") String inventoryServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public InventoryResponse getInventoryByProductId(
            Long productId) {

        return restClient
                .get()
                .uri(
                        "/api/inventory/product/{productId}",
                        productId
                )
                .retrieve()
                .body(InventoryResponse.class);
    }

    public InventoryResponse reserveInventory(
            Long inventoryId,
            Integer quantity) {

        InventoryQuantityRequest request =
                new InventoryQuantityRequest(quantity);

        return restClient
                .post()
                .uri(
                        "/api/inventory/{id}/reserve",
                        inventoryId
                )
                .body(request)
                .retrieve()
                .body(InventoryResponse.class);
    }

    public record InventoryQuantityRequest(
            Integer quantity) {
    }

    public static class InventoryResponse {

        private Long id;
        private Long productId;
        private Integer availableQuantity;
        private Integer reservedQuantity;

        public InventoryResponse() {
        }

        public Long getId() {
            return id;
        }

        public Long getProductId() {
            return productId;
        }

        public Integer getAvailableQuantity() {
            return availableQuantity;
        }

        public Integer getReservedQuantity() {
            return reservedQuantity;
        }
    }
}