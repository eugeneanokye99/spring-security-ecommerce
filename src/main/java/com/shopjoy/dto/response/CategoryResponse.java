package com.shopjoy.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "Category response containing category details and hierarchy information")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    
    @Schema(description = "Category unique identifier", example = "1")
    private Integer categoryId;
    
    @Schema(description = "Category name", example = "Laptops")
    private String categoryName;
    
    @Schema(description = "Category description", example = "Portable computers for personal and business use")
    private String description;
    
    @Schema(description = "Parent category ID (null if top-level category)", example = "1")
    private Integer parentCategoryId;
    
    @Schema(description = "Category creation timestamp", example = "2024-01-20T10:30:00")
    private LocalDateTime createdAt;

}
