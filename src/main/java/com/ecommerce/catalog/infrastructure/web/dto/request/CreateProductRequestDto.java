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
 * DTO para la creación de productos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDto {

    @JsonProperty("title")
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 255, message = "El título debe tener entre 5 y 255 caracteres")
    private String title;

    @JsonProperty("description")
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String description;

    @JsonProperty("price")
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal price;

    @JsonProperty("currency_id")
    @NotBlank(message = "El ID de moneda es obligatorio")
    @Pattern(regexp = "^(ARS|USD|EUR|BRL)$", message = "Moneda debe ser ARS, USD, EUR o BRL")
    private String currencyId;

    @JsonProperty("condition")
    @NotBlank(message = "La condición es obligatoria")
    @Pattern(regexp = "^(new|used|not_specified)$", message = "Condición debe ser: new, used o not_specified")
    private String condition;

    @JsonProperty("thumbnail")
    @NotBlank(message = "La imagen principal es obligatoria")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|webp)$", message = "La URL debe ser válida y terminar en jpg, jpeg, png o webp")
    private String thumbnail;

    @JsonProperty("pictures")
    @Valid
    @Size(min = 1, max = 10, message = "Debe incluir entre 1 y 10 imágenes")
    private List<CreatePictureRequestDto> pictures;

    @JsonProperty("attributes")
    @Valid
    @Size(min = 1, max = 20, message = "Debe incluir entre 1 y 20 atributos")
    private List<CreateAttributeRequestDto> attributes;

    @JsonProperty("variations")
    @Valid
    @Size(max = 50, message = "Máximo 50 variaciones permitidas")
    private List<CreateVariationRequestDto> variations;
}