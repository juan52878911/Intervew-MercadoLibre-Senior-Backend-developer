package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entidad para las variaciones del producto (talle, color, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariationDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;

    @JsonProperty("attribute_combinations")
    private List<AttributeCombinationDto> attributeCombinations;
}