package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entidad para las combinaciones de atributos en variaciones
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeCombinationDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("value_name")
    private String valueName;
}