package com.shopsphere.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopsphere.inventory.dto.InventoryAvailabilityResponse;
import com.shopsphere.inventory.dto.InventoryQuantityRequest;
import com.shopsphere.inventory.dto.InventoryRequest;
import com.shopsphere.inventory.dto.InventoryResponse;
import com.shopsphere.inventory.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(request));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(id)
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByProductId(productId)
        );
    }

    @GetMapping("/product/{productId}/availability")
    public ResponseEntity<InventoryAvailabilityResponse> getAvailability(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getAvailability(productId)
        );
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<InventoryResponse> reserveInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return ResponseEntity.ok(
                inventoryService.reserveInventory(id, request)
        );
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<InventoryResponse> releaseInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryQuantityRequest request) {

        return ResponseEntity.ok(
                inventoryService.releaseInventory(id, request)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {

        return ResponseEntity.ok(
                inventoryService.updateInventory(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(@PathVariable Long id) {

        inventoryService.deleteInventory(id);
    }
}