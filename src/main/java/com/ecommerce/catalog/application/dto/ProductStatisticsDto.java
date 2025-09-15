package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatisticsDto {
    private long totalProducts;
    private long activeProducts;
    private long totalBrands;
    private long totalCategories;
    private long productsWithVariations;
    private List<String> brands;
    private List<String> categories;
}