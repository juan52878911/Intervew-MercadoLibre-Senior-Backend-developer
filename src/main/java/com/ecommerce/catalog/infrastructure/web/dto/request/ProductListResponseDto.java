package com.ecommerce.catalog.infrastructure.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO wrapper para respuestas de listado de productos
 * Incluye paginación y metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponseDto {

    @JsonProperty("site_id")
    private String siteId;

    @JsonProperty("query")
    private String query;

    @JsonProperty("paging")
    private PagingResponseDto paging;

    @JsonProperty("results")
    private List<ProductSummaryResponseDto> results;

    @JsonProperty("sort")
    private SortResponseDto sort;

    @JsonProperty("available_sorts")
    private List<SortResponseDto> availableSorts;
}