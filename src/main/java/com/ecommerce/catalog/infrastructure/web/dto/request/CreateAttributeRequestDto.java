package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear atributos del producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAttributeRequestDto {

    @JsonProperty("id")
    @NotBlank(message = "El ID del atributo es obligatorio")
    @Size(min = 2, max = 50, message = "El ID debe tener entre 2 y 50 caracteres")
    private String id;

    @JsonProperty("name")
    @NotBlank(message = "El nombre del atributo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;

    @JsonProperty("value_name")
    @NotBlank(message = "El valor del atributo es obligatorio")
    @Size(min = 1, max = 200, message = "El valor debe tener entre 1 y 200 caracteres")
    private String valueName;
}