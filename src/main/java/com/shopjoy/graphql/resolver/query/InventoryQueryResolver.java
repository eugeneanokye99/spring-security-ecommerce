package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.response.InventoryResponse;
import com.shopjoy.service.InventoryService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class InventoryQueryResolver {

    private final InventoryService inventoryService;

    public InventoryQueryResolver(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryResponse> lowStockProducts() {
        return inventoryService.getLowStockProducts();
    }
}
