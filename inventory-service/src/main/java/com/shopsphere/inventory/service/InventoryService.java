package com.shopsphere.inventory.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.inventory.dto.InventoryAvailabilityResponse;
import com.shopsphere.inventory.dto.InventoryQuantityRequest;
import com.shopsphere.inventory.dto.InventoryRequest;
import com.shopsphere.inventory.dto.InventoryResponse;
import com.shopsphere.inventory.entity.Inventory;
import com.shopsphere.inventory.exception.InventoryNotFoundException;
import com.shopsphere.inventory.repository.InventoryRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryResponse createInventory(InventoryRequest request) {

        LocalDateTime now = LocalDateTime.now();

        Inventory inventory = new Inventory();

        inventory.setProductId(request.productId());
        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setReservedQuantity(request.reservedQuantity());
        inventory.setCreatedAt(now);
        inventory.setUpdatedAt(now);

        Inventory savedInventory = inventoryRepository.save(inventory);

        return toResponse(savedInventory);
    }

    public List<InventoryResponse> getAllInventory() {

        return inventoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InventoryResponse getInventoryById(Long id) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new InventoryNotFoundException(id)
                );

        return toResponse(inventory);
    }

    public InventoryResponse getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        return toResponse(inventory);
    }

    public InventoryAvailabilityResponse getAvailability(Long productId) {

        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(productId)
                );

        Integer availableQuantity = inventory.getAvailableQuantity();
        Integer reservedQuantity = inventory.getReservedQuantity();

        Integer totalQuantity =
                availableQuantity + reservedQuantity;

        return new InventoryAvailabilityResponse(
                inventory.getProductId(),
                availableQuantity,
                reservedQuantity,
                totalQuantity,
                availableQuantity > 0
        );
    }

    public InventoryResponse updateInventory(
            Long id,
            InventoryRequest request) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new InventoryNotFoundException(id)
                );

        inventory.setProductId(request.productId());
        inventory.setAvailableQuantity(request.availableQuantity());
        inventory.setReservedQuantity(request.reservedQuantity());
        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory updatedInventory =
                inventoryRepository.save(inventory);

        return toResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse reserveInventory(
            Long id,
            InventoryQuantityRequest request) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new InventoryNotFoundException(id)
                );

        int quantity = request.quantity();

        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient available inventory"
            );
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity
        );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity
        );

        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory updatedInventory =
                inventoryRepository.save(inventory);

        return toResponse(updatedInventory);
    }

    @Transactional
    public InventoryResponse releaseInventory(
            Long id,
            InventoryQuantityRequest request) {

        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new InventoryNotFoundException(id)
                );

        int quantity = request.quantity();

        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Cannot release more inventory than reserved"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity
        );

        inventory.setUpdatedAt(LocalDateTime.now());

        Inventory updatedInventory =
                inventoryRepository.save(inventory);

        return toResponse(updatedInventory);
    }

    public void deleteInventory(Long id) {

        if (!inventoryRepository.existsById(id)) {
            throw new InventoryNotFoundException(id);
        }

        inventoryRepository.deleteById(id);
    }

    private InventoryResponse toResponse(Inventory inventory) {

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}