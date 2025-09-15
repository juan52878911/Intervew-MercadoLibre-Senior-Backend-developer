package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para el listado de productos (versión simplificada)
 * Usado para búsquedas y listados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponseDto {

    @JsonProperty("id")
    @NotBlank
    private String id;

    @JsonProperty("title")
    @NotBlank
    private String title;

    @JsonProperty("price")
    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @JsonProperty("currency_id")
    @NotBlank
    private String currencyId;

    @JsonProperty("available_quantity")
    private Integer availableQuantity;

    @JsonProperty("sold_quantity")
    private Integer soldQuantity;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("permalink")
    private String permalink;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("seller_id")
    private Long sellerId;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("status")
    private String status;

    @JsonProperty("shipping")
    private ShippingResponseDto shipping;
}