package com.shopjoy.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilter {
    private String searchTerm;
    private Integer categoryId;
    private Double minPrice;
    private Double maxPrice;
    private Boolean active;
    private String brand;
    private Boolean inStock;
    private Integer minStock;
    private Integer maxStock;
}
