package com.ecommerce.catalog.infrastructure.web;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.application.service.ProductService;
import com.ecommerce.catalog.infrastructure.web.dto.request.*;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para gestión de productos
 * Expone endpoints tipo MercadoLibre API
 */
@Path("/api/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ProductController {

    private final ProductService productService;

    @Inject
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ================================
    // CREATE ENDPOINTS
    // ================================

    @POST
    public Response createProduct(@Valid CreateProductRequestDto request) {
        log.info("POST /api/items - Creando producto: {}", request.getTitle());

        ProductDto product = productService.createProduct(request);

        return Response.status(Response.Status.CREATED)
                .entity(product)
                .build();
    }

    @POST
    @Path("/batch")
    public Response createProductsBatch(@Valid List<CreateProductRequestDto> requests) {
        log.info("POST /api/items/batch - Creando {} productos", requests.size());

        List<ProductDto> products = productService.createProducts(requests);

        return Response.status(Response.Status.CREATED)
                .entity(products)
                .build();
    }

    // ================================
    // READ ENDPOINTS
    // ================================

    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") String id) {
        log.debug("GET /api/items/{} - Obteniendo producto", id);

        ProductDto product = productService.getProductById(id);

        return Response.ok(product).build();
    }

    @GET
    public Response getAllProducts(
            @QueryParam("offset") @DefaultValue("0") @Min(0) int offset,
            @QueryParam("limit") @DefaultValue("50") @Min(1) @Max(200) int limit,
            @QueryParam("sort") String sortBy) {
        log.debug("GET /api/items - Offset: {}, Limit: {}, Sort: {}", offset, limit, sortBy);

        ProductListResponseDto response = productService.getAllProducts(offset, limit, sortBy);

        return Response.ok(response).build();
    }

    @GET
    @Path("/search")
    public Response searchProducts(
            @QueryParam("q") String query,
            @QueryParam("brand") String brand,
            @QueryParam("price_min") BigDecimal minPrice,
            @QueryParam("price_max") BigDecimal maxPrice,
            @QueryParam("condition") String condition,
            @QueryParam("offset") @DefaultValue("0") @Min(0) int offset,
            @QueryParam("limit") @DefaultValue("50") @Min(1) @Max(200) int limit,
            @QueryParam("sort") String sortBy) {

        log.info("GET /api/items/search - Query: '{}', Brand: '{}', Price: {}-{}",
                query, brand, minPrice, maxPrice);

        ProductListResponseDto response = productService.advancedSearch(
                query, brand, minPrice, maxPrice, condition, offset, limit, sortBy);

        return Response.ok(response).build();
    }

    @GET
    @Path("/search/title")
    public Response searchByTitle(@QueryParam("title") String title) {
        log.debug("GET /api/items/search/title - Title: '{}'", title);

        List<ProductDto> products = productService.searchByTitle(title);

        return Response.ok(products).build();
    }

    @GET
    @Path("/search/brand/{brand}")
    public Response searchByBrand(@PathParam("brand") String brand) {
        log.debug("GET /api/items/search/brand/{} - Buscando productos", brand);

        List<ProductDto> products = productService.searchByBrand(brand);

        return Response.ok(products).build();
    }

    @GET
    @Path("/search/price")
    public Response searchByPriceRange(
            @QueryParam("min") BigDecimal minPrice,
            @QueryParam("max") BigDecimal maxPrice,
            @QueryParam("currency") String currency) {

        log.debug("GET /api/items/search/price - Range: {}-{} {}", minPrice, maxPrice, currency);

        List<ProductDto> products = productService.searchByPriceRange(minPrice, maxPrice, currency);

        return Response.ok(products).build();
    }

    // ================================
    // UPDATE ENDPOINTS
    // ================================

    @PUT
    @Path("/{id}")
    public Response updateProduct(
            @PathParam("id") String id,
            @Valid UpdateProductRequestDto request) {

        log.info("PUT /api/items/{} - Actualizando producto", id);

        ProductDto product = productService.updateProduct(id, request);

        return Response.ok(product).build();
    }

    @PUT
    @Path("/{id}/price")
    public Response updatePrice(
            @PathParam("id") String id,
            @QueryParam("price") BigDecimal newPrice,
            @QueryParam("reason") @DefaultValue("Actualización manual") String reason) {

        log.info("PUT /api/items/{}/price - Nuevo precio: {}", id, newPrice);

        ProductDto product = productService.updatePrice(id, newPrice, reason);

        return Response.ok(product).build();
    }

    @PUT
    @Path("/{id}/status")
    public Response updateStatus(
            @PathParam("id") String id,
            @QueryParam("status") String newStatus) {

        log.info("PUT /api/items/{}/status - Nuevo estado: {}", id, newStatus);

        ProductDto product = productService.updateStatus(id, newStatus);

        return Response.ok(product).build();
    }

    // ================================
    // DELETE ENDPOINTS
    // ================================

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") String id) {
        log.info("DELETE /api/items/{} - Eliminando producto", id);

        boolean deleted = productService.deleteProduct(id);

        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/batch")
    public Response deleteProductsBatch(List<String> productIds) {
        log.info("DELETE /api/items/batch - Eliminando {} productos", productIds.size());

        BatchOperationResultDto result = productService.deleteProducts(productIds);

        return Response.ok(result).build();
    }

    // ================================
    // ANALYTICS ENDPOINTS
    // ================================

    @GET
    @Path("/statistics")
    public Response getStatistics() {
        log.debug("GET /api/items/statistics - Obteniendo estadísticas");

        ProductStatisticsDto statistics = productService.getStatistics();

        return Response.ok(statistics).build();
    }

    @GET
    @Path("/sort-options")
    public Response getSortOptions() {
        log.debug("GET /api/items/sort-options - Obteniendo opciones de ordenamiento");

        List<SortResponseDto> sortOptions = productService.getAvailableSortOptions();

        return Response.ok(sortOptions).build();
    }

    // ================================
    // UTILITY ENDPOINTS
    // ================================

    @GET
    @Path("/brands")
    public Response getAvailableBrands() {
        log.debug("GET /api/items/brands - Obteniendo marcas disponibles");

        ProductStatisticsDto statistics = productService.getStatistics();

        return Response.ok(statistics.getBrands()).build();
    }

    @GET
    @Path("/categories")
    public Response getAvailableCategories() {
        log.debug("GET /api/items/categories - Obteniendo categorías disponibles");

        ProductStatisticsDto statistics = productService.getStatistics();

        return Response.ok(statistics.getCategories()).build();
    }
}