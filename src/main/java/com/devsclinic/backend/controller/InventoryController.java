package com.devsclinic.backend.controller;

import com.devsclinic.backend.dto.InventoryItemRequest;
import com.devsclinic.backend.model.InventoryActivity;
import com.devsclinic.backend.model.InventoryItem;
import com.devsclinic.backend.service.InventoryActivityService;
import com.devsclinic.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryActivityService inventoryActivityService;

    public InventoryController(InventoryService inventoryService, InventoryActivityService inventoryActivityService) {
        this.inventoryService = inventoryService;
        this.inventoryActivityService = inventoryActivityService;
    }
    @GetMapping
    public List<InventoryItem> getAll() {
        return inventoryService.getAll();
    }
    @GetMapping("/activities")
    public List<InventoryActivity> getRecentActivities(@RequestParam(defaultValue = "20") int limit) {
        return inventoryActivityService.getRecent(limit);
    }
    @GetMapping("/{id}")
    public InventoryItem getById(@PathVariable String id) {
        return inventoryService.getById(id);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItem create(@Valid @RequestBody InventoryItemRequest request) {
        return inventoryService.create(request);
    }
    @PutMapping("/{id}")
    public InventoryItem update(@PathVariable String id, @Valid @RequestBody InventoryItemRequest request) {
        return inventoryService.update(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        inventoryService.delete(id);
    }
}
