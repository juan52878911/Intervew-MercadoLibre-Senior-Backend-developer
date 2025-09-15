package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO para información de envío del producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingResponseDto {

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("local_pick_up")
    private Boolean localPickUp;

    @JsonProperty("free_shipping")
    private Boolean freeShipping;

    @JsonProperty("logistic_type")
    private String logisticType;

    @JsonProperty("store_pick_up")
    private Boolean storePickUp;

    @JsonProperty("tags")
    private List<String> tags;

    public ShippingResponseDto(String mode, Boolean freeShipping) {
        this.mode = mode;
        this.freeShipping = freeShipping;
    }
}