package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para crear imágenes en el producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePictureRequestDto {

    @JsonProperty("url")
    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|webp)$", message = "La URL debe ser válida y terminar en jpg, jpeg, png o webp")
    private String url;

    @JsonProperty("secure_url")
    @NotBlank(message = "La URL segura es obligatoria")
    @Pattern(regexp = "^https://.*\\.(jpg|jpeg|png|webp)$", message = "La URL segura debe usar HTTPS y terminar en jpg, jpeg, png o webp")
    private String secureUrl;
}
