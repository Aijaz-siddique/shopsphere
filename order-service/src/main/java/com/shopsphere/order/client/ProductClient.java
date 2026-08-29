package com.shopsphere.order.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.shopsphere.order.exception.DownstreamServiceException;

@Component
public class ProductClient {

    private static final String SERVICE_NAME = "product-service";

    private final RestClient restClient;

    public ProductClient(
            RestClient.Builder restClientBuilder,
            @Value("${product-service.url}") String productServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(productServiceUrl)
                .build();
    }

    public ProductResponse getProduct(Long productId) {

        return restClient
                .get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, response) -> {
                            String message = response.getBody() != null
                                    ? new String(response.getBody().readAllBytes())
                                    : "Product service request failed";

                            throw new DownstreamServiceException(
                                    SERVICE_NAME,
                                    response.getStatusCode().value(),
                                    message
                            );
                        }
                )
                .body(ProductResponse.class);
    }

    public static class ProductResponse {

        private Long id;
        private String name;
        private BigDecimal price;

        public ProductResponse() {
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }
    }
}
