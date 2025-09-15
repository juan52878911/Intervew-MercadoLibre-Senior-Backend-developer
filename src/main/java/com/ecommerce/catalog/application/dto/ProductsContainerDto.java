package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Contenedor para deserializar el archivo JSON que contiene la lista de productos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductsContainerDto {

    @JsonProperty("products")
    private List<ProductDto> products;
}
