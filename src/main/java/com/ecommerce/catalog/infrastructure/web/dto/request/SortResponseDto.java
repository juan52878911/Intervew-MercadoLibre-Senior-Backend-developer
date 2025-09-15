package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para opciones de ordenamiento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortResponseDto {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("active")
    private Boolean active;

    public SortResponseDto(String id, String name) {
        this.id = id;
        this.name = name;
        this.active = false;
    }
}