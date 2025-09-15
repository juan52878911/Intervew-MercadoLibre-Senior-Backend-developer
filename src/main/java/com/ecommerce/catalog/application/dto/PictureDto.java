package com.ecommerce.catalog.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entidad para las imágenes del producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PictureDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("secure_url")
    private String secureUrl;
}