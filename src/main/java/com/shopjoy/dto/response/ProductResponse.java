package com.shopjoy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Product response containing all product details")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    @Schema(description = "Product unique identifier", example = "1")
    private Integer id;

    @Schema(description = "Product name", example = "Dell XPS 15 Laptop")
    private String productName;

    @Schema(description = "Detailed product description", example = "High-performance laptop with Intel Core i7 processor")
    private String description;

    @Schema(description = "Category unique identifier", example = "1")
    private Integer categoryId;

    @Schema(description = "Category name", example = "Electronics")
    private String categoryName;

    @Schema(description = "Current stock quantity", example = "50")
    private int stockQuantity;

    @Schema(description = "Stock reorder level threshold", example = "10")
    private int reorderLevel;

    @Schema(description = "Product selling price", example = "1299.99")
    private double price;

    @Schema(description = "Product cost price", example = "899.99")
    private double costPrice;

    @Schema(description = "Stock Keeping Unit", example = "LAPTOP-DELL-XPS15-001")
    private String sku;

    @Schema(description = "Product brand name", example = "Dell")
    private String brand;

    @Schema(description = "Product image URL", example = "https://example.com/images/dell-xps15.jpg")
    private String imageUrl;

    @Schema(description = "Whether the product is active and available", example = "true")
    private boolean isActive;

    @Schema(description = "Product creation timestamp", example = "2024-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Product last update timestamp", example = "2024-01-20T15:45:00")
    private LocalDateTime updatedAt;

}
