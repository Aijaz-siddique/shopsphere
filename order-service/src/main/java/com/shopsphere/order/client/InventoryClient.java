package com.shopsphere.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.shopsphere.order.exception.DownstreamServiceException;

@Component
public class InventoryClient {

    private static final String SERVICE_NAME = "inventory-service";

    private final RestClient restClient;

    public InventoryClient(
            RestClient.Builder restClientBuilder,
            @Value("${inventory-service.url}") String inventoryServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(inventoryServiceUrl)
                .build();
    }

    public InventoryResponse getInventoryByProductId(Long productId) {

        return restClient
                .get()
                .uri("/api/inventory/product/{productId}", productId)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, response) -> throwDownstreamException(
                                "Inventory lookup failed",
                                response
                        )
                )
                .body(InventoryResponse.class);
    }

    public InventoryResponse reserveInventory(
            Long inventoryId,
            Integer quantity) {

        return changeInventory(
                inventoryId,
                quantity,
                "/api/inventory/{id}/reserve",
                "Inventory reservation failed"
        );
    }

    public InventoryResponse releaseInventory(
            Long inventoryId,
            Integer quantity) {

        return changeInventory(
                inventoryId,
                quantity,
                "/api/inventory/{id}/release",
                "Inventory release failed"
        );
    }

    private InventoryResponse changeInventory(
            Long inventoryId,
            Integer quantity,
            String uri,
            String failureMessage) {

        InventoryQuantityRequest request =
                new InventoryQuantityRequest(quantity);

        return restClient
                .post()
                .uri(uri, inventoryId)
                .body(request)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (requestMessage, response) -> throwDownstreamException(
                                failureMessage,
                                response
                        )
                )
                .body(InventoryResponse.class);
    }

    private void throwDownstreamException(
            String defaultMessage,
            org.springframework.http.client.ClientHttpResponse response) {

        String message = response.getStatusText();

        if (message == null || message.isBlank()) {
            message = defaultMessage;
        }

        throw new DownstreamServiceException(
                SERVICE_NAME,
                response.getStatusCode().value(),
                message
        );
    }

    public record InventoryQuantityRequest(Integer quantity) {
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
