package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para crear variaciones del producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariationRequestDto {

    @JsonProperty("price")
    @NotNull(message = "El precio de la variación es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal price;

    @JsonProperty("available_quantity")
    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Max(value = 99999, message = "La cantidad máxima es 99999")
    private Integer availableQuantity;

    @JsonProperty("attribute_combinations")
    @Valid
    @NotEmpty(message = "Debe incluir al menos una combinación de atributos")
    @Size(min = 1, max = 10, message = "Entre 1 y 10 combinaciones de atributos")
    private List<CreateAttributeCombinationRequestDto> attributeCombinations;
}