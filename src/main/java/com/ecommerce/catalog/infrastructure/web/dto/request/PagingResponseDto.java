package com.ecommerce.catalog.infrastructure.web.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para información de paginación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingResponseDto {

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("primary_results")
    private Integer primaryResults;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("has_next_page")
    private Boolean hasNextPage;

    @JsonProperty("has_previous_page")
    private Boolean hasPreviousPage;

    @JsonProperty("next_offset")
    private Integer nextOffset;

    @JsonProperty("previous_offset")
    private Integer previousOffset;

    // Constructor adicional que calcula automáticamente los campos de navegación
    public PagingResponseDto(Integer total, Integer offset, Integer limit) {
        this.total = total;
        this.offset = offset;
        this.limit = limit;
        this.primaryResults = total;

        // Calcular navegación
        this.hasNextPage = (offset + limit) < total;
        this.hasPreviousPage = offset > 0;
        this.nextOffset = hasNextPage ? offset + limit : null;
        this.previousOffset = hasPreviousPage ? Math.max(0, offset - limit) : null;
    }
}