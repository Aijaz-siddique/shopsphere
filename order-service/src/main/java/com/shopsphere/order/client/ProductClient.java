package com.shopsphere.order.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

        try {
            return restClient
                    .get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            (request, response) -> {
                                throw new DownstreamServiceException(
                                        SERVICE_NAME,
                                        response.getStatusCode().value(),
                                        "Product service returned "
                                                + response.getStatusCode().value()
                                );
                            }
                    )
                    .body(ProductResponse.class);
        } catch (DownstreamServiceException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new DownstreamServiceException(
                    SERVICE_NAME,
                    503,
                    "Product service is unavailable"
            );
        }
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
