package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad de dominio para Producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("currency_id")
    private String currencyId;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("status")
    private String status;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("permalink")
    private String permalink;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;

    @JsonProperty("pictures")
    private List<PictureDto> pictures;

    @JsonProperty("attributes")
    private List<AttributeDto> attributes;

    @JsonProperty("variations")
    private List<VariationDto> variations;
}