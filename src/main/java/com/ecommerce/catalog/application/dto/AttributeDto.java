package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entidad para los atributos del producto (marca, género, material, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value_name")
    private String valueName;
}