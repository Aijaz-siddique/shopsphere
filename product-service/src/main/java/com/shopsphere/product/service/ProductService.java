package com.shopsphere.product.service;

import com.shopsphere.product.dto.ProductRequest;
import com.shopsphere.product.dto.ProductResponse;
import com.shopsphere.product.entity.Product;
import com.shopsphere.product.exception.ProductNotFoundException;
import com.shopsphere.product.repository.ProductRepository;


import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

        private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
                "id",
                "name",
                "price",
                "createdAt",
                "updatedAt"
        );

    public ProductResponse createProduct(ProductRequest request) {

        LocalDateTime now = LocalDateTime.now();

        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                now,
                now
        );

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    public Page<ProductResponse> getAllProducts(
        int page,
        int size,
        String sortBy,
        String direction,
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice) {

        if (page < 0) {
                throw new IllegalArgumentException(
                        "Page must be greater than or equal to 0"
                );
        }

        if (size < 1 || size > 100) {
                throw new IllegalArgumentException(
                        "Size must be between 1 and 100"
                );
        }

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
                throw new IllegalArgumentException(
                        "Invalid sort field: " + sortBy
                );
        }

        if (!direction.equalsIgnoreCase("asc")
                && !direction.equalsIgnoreCase("desc")) {

                throw new IllegalArgumentException(
                        "Direction must be either 'asc' or 'desc'"
                );
        }

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Minimum price cannot be negative"
                );
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Maximum price cannot be negative"
                );
        }

        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
                throw new IllegalArgumentException(
                        "Minimum price cannot be greater than maximum price"
                );
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        String normalizedSearch =
                search == null || search.isBlank()
                        ? null
                        : search.trim();

        return productRepository.searchProducts(
                normalizedSearch,
                minPrice,
                maxPrice,
                pageable
        ).map(this::toResponse);
        
    }
       
    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        return toResponse(product);
    }

        public ProductResponse updateProduct(Long id, ProductRequest request) {

                Product product = productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(id)
                        );

                product.setName(request.name());
                product.setDescription(request.description());
                product.setPrice(request.price());
                product.setUpdatedAt(LocalDateTime.now());

                Product updatedProduct = productRepository.save(product);

                return toResponse(updatedProduct);
        }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}