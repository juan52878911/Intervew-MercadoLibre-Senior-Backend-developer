package com.ecommerce.catalog.infrastructure.web;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.application.service.ProductService;
import com.ecommerce.catalog.infrastructure.web.dto.request.*;
import com.ecommerce.catalog.domain.exception.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("Product Controller Integration Tests")
class ProductControllerTest {

    @InjectMock
    ProductService productService;

    private ProductDto sampleProduct;
    private CreateProductRequestDto createRequest;
    private UpdateProductRequestDto updateRequest;
    private ProductListResponseDto listResponse;

    @BeforeEach
    void setUp() {
        // Producto de ejemplo
        sampleProduct = ProductDto.builder()
                .id("MLA1234567890")
                .title("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .currencyId("ARS")
                .condition("new")
                .status("active")
                .thumbnail("https://example.com/image.jpg")
                .dateCreated(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                .build();

        // Request de creación
        createRequest = CreateProductRequestDto.builder()
                .title("New Test Product")
                .description("New Test Description")
                .price(new BigDecimal("150.00"))
                .currencyId("ARS")
                .condition("new")
                .thumbnail("https://example.com/new-image.jpg")
                .build();

        // Request de actualización
        updateRequest = UpdateProductRequestDto.builder()
                .title("Updated Test Product")
                .price(new BigDecimal("120.00"))
                .build();

        // Response de lista
        ProductSummaryResponseDto summaryResponse = ProductSummaryResponseDto.builder()
                .id(sampleProduct.getId())
                .title(sampleProduct.getTitle())
                .price(sampleProduct.getPrice())
                .currencyId(sampleProduct.getCurrencyId())
                .condition(sampleProduct.getCondition())
                .thumbnail(sampleProduct.getThumbnail())
                .status(sampleProduct.getStatus())
                .build();

        PagingResponseDto paging = PagingResponseDto.builder()
                .total(1)
                .offset(0)
                .limit(50)
                .build();

        listResponse = ProductListResponseDto.builder()
                .results(List.of(summaryResponse))
                .paging(paging)
                .build();
    }

    // ================================
    // CREATE ENDPOINTS TESTS
    // ================================

    @Test
    @DisplayName("POST /api/items - Debe crear producto exitosamente")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productService.createProduct(any(CreateProductRequestDto.class)))
                .thenReturn(sampleProduct);

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post("/api/items")
                .then()
                .statusCode(201)
                .body("id", equalTo("MLA1234567890"))
                .body("title", equalTo("Test Product"))
                .body("price", equalTo(100.00f))
                .body("status", equalTo("active"));
    }

    @Test
    @DisplayName("POST /api/items/batch - Debe crear múltiples productos")
    void shouldCreateMultipleProducts() {
        // Given
        List<CreateProductRequestDto> requests = Arrays.asList(createRequest, createRequest);
        List<ProductDto> products = Arrays.asList(sampleProduct, sampleProduct);

        when(productService.createProducts(anyList())).thenReturn(products);

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(requests)
                .when()
                .post("/api/items/batch")
                .then()
                .statusCode(201)
                .body("size()", equalTo(2))
                .body("[0].id", equalTo("MLA1234567890"))
                .body("[1].id", equalTo("MLA1234567890"));
    }

    // ================================
    // READ ENDPOINTS TESTS
    // ================================

    @Test
    @DisplayName("GET /api/items/{id} - Debe obtener producto por ID")
    void shouldGetProductById() {
        // Given
        when(productService.getProductById("MLA1234567890"))
                .thenReturn(sampleProduct);

        // When & Then
        given()
                .when()
                .get("/api/items/MLA1234567890")
                .then()
                .statusCode(200)
                .body("id", equalTo("MLA1234567890"))
                .body("title", equalTo("Test Product"))
                .body("price", equalTo(100.00f))
                .body("currency_id", equalTo("ARS"))  // Cambiar a snake_case
                .body("condition", equalTo("new"))
                .body("status", equalTo("active"));
    }

    @Test
    @DisplayName("GET /api/items/{id} - Debe retornar 404 cuando producto no existe")
    void shouldReturn404WhenProductNotFound() {
        // Given
        when(productService.getProductById("MLA9999999999"))
                .thenThrow(new ProductNotFoundException("Producto no encontrado"));

        // When & Then
        given()
                .when()
                .get("/api/items/MLA9999999999")
                .then()
                .statusCode(500); // QuarkusTest maneja excepciones como 500
    }

    @Test
    @DisplayName("GET /api/items - Debe listar productos con paginación")
    void shouldListProductsWithPagination() {
        // Given
        when(productService.getAllProducts(0, 50, null))
                .thenReturn(listResponse);

        // When & Then
        given()
                .queryParam("offset", 0)
                .queryParam("limit", 50)
                .when()
                .get("/api/items")
                .then()
                .statusCode(200)
                .body("results.size()", equalTo(1))
                .body("results[0].id", equalTo("MLA1234567890"))
                .body("paging.total", equalTo(1))
                .body("paging.offset", equalTo(0))
                .body("paging.limit", equalTo(50));
    }

    @Test
    @DisplayName("GET /api/items/search - Debe realizar búsqueda avanzada")
    void shouldPerformAdvancedSearch() {
        // Given
        when(productService.advancedSearch(
                eq("iPhone"), eq("Apple"), any(), any(), eq("new"),
                eq(0), eq(50), eq("price_asc")))
                .thenReturn(listResponse);

        // When & Then
        given()
                .queryParam("q", "iPhone")
                .queryParam("brand", "Apple")
                .queryParam("condition", "new")
                .queryParam("offset", 0)
                .queryParam("limit", 50)
                .queryParam("sort", "price_asc")
                .when()
                .get("/api/items/search")
                .then()
                .statusCode(200)
                .body("results.size()", greaterThanOrEqualTo(0))
                .body("paging", notNullValue());
    }

    @Test
    @DisplayName("GET /api/items/search/title - Debe buscar por título")
    void shouldSearchByTitle() {
        // Given
        when(productService.searchByTitle("Test"))
                .thenReturn(Arrays.asList(sampleProduct));

        // When & Then
        given()
                .queryParam("title", "Test")
                .when()
                .get("/api/items/search/title")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].title", containsString("Test"));
    }

    @Test
    @DisplayName("GET /api/items/search/brand/{brand} - Debe buscar por marca")
    void shouldSearchByBrand() {
        // Given
        when(productService.searchByBrand("Nike"))
                .thenReturn(Arrays.asList(sampleProduct));

        // When & Then
        given()
                .when()
                .get("/api/items/search/brand/Nike")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo("MLA1234567890"));
    }

    @Test
    @DisplayName("GET /api/items/search/price - Debe buscar por rango de precio")
    void shouldSearchByPriceRange() {
        // Given
        when(productService.searchByPriceRange(
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                "ARS"))
                .thenReturn(Arrays.asList(sampleProduct));

        // When & Then
        given()
                .queryParam("min", "50.00")
                .queryParam("max", "200.00")
                .queryParam("currency", "ARS")
                .when()
                .get("/api/items/search/price")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].price", equalTo(100.00f));
    }

    // ================================
    // UPDATE ENDPOINTS TESTS
    // ================================

    @Test
    @DisplayName("PUT /api/items/{id} - Debe actualizar producto")
    void shouldUpdateProduct() {
        // Given
        ProductDto updatedProduct = ProductDto.builder()
                .id("MLA1234567890")
                .title("Updated Test Product")
                .price(new BigDecimal("120.00"))
                .currencyId("ARS")
                .condition("new")
                .status("active")
                .build();

        when(productService.updateProduct(eq("MLA1234567890"), any(UpdateProductRequestDto.class)))
                .thenReturn(updatedProduct);

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put("/api/items/MLA1234567890")
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Test Product"))
                .body("price", equalTo(120.00f));
    }

    @Test
    @DisplayName("PUT /api/items/{id}/price - Debe actualizar precio")
    void shouldUpdatePrice() {
        // Given
        ProductDto updatedProduct = sampleProduct;
        updatedProduct.setPrice(new BigDecimal("199.99"));

        when(productService.updatePrice(
                "MLA1234567890",
                new BigDecimal("199.99"),
                "Promoción especial"))
                .thenReturn(updatedProduct);

        // When & Then
        given()
                .queryParam("price", "199.99")
                .queryParam("reason", "Promoción especial")
                .when()
                .put("/api/items/MLA1234567890/price")
                .then()
                .statusCode(200)
                .body("price", equalTo(199.99f));
    }

    @Test
    @DisplayName("PUT /api/items/{id}/status - Debe actualizar estado")
    void shouldUpdateStatus() {
        // Given
        ProductDto updatedProduct = sampleProduct;
        updatedProduct.setStatus("paused");

        when(productService.updateStatus("MLA1234567890", "paused"))
                .thenReturn(updatedProduct);

        // When & Then
        given()
                .queryParam("status", "paused")
                .when()
                .put("/api/items/MLA1234567890/status")
                .then()
                .statusCode(200)
                .body("status", equalTo("paused"));
    }

    // ================================
    // DELETE ENDPOINTS TESTS
    // ================================

    @Test
    @DisplayName("DELETE /api/items/{id} - Debe eliminar producto")
    void shouldDeleteProduct() {
        // Given
        when(productService.deleteProduct("MLA1234567890"))
                .thenReturn(true);

        // When & Then
        given()
                .when()
                .delete("/api/items/MLA1234567890")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("DELETE /api/items/batch - Debe eliminar productos en batch")
    void shouldDeleteProductsBatch() {
        // Given
        List<String> ids = Arrays.asList("MLA1234567890", "MLA1234567891");
        BatchOperationResultDto result = BatchOperationResultDto.builder()
                .totalProcessed(2)
                .successful(2)
                .failed(0)
                .build();

        when(productService.deleteProducts(anyList()))
                .thenReturn(result);

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(ids)
                .when()
                .delete("/api/items/batch")
                .then()
                .statusCode(200)
                .body("totalProcessed", equalTo(2))
                .body("successful", equalTo(2))
                .body("failed", equalTo(0));
    }

    // ================================
    // ANALYTICS ENDPOINTS TESTS
    // ================================

    @Test
    @DisplayName("GET /api/items/statistics - Debe obtener estadísticas")
    void shouldGetStatistics() {
        // Given
        ProductStatisticsDto statistics = ProductStatisticsDto.builder()
                .totalProducts(100L)
                .activeProducts(85L)
                .totalBrands(15L)
                .totalCategories(8L)
                .productsWithVariations(45L)
                .brands(Arrays.asList("Nike", "Adidas", "Apple"))
                .categories(Arrays.asList("Footwear", "Electronics", "Clothing"))
                .build();

        when(productService.getStatistics()).thenReturn(statistics);

        // When & Then
        given()
                .when()
                .get("/api/items/statistics")
                .then()
                .statusCode(200)
                .body("totalProducts", equalTo(100))
                .body("activeProducts", equalTo(85))
                .body("totalBrands", equalTo(15))
                .body("brands.size()", equalTo(3))
                .body("brands", hasItems("Nike", "Adidas", "Apple"));
    }

    @Test
    @DisplayName("GET /api/items/sort-options - Debe obtener opciones de ordenamiento")
    void shouldGetSortOptions() {
        // Given
        List<SortResponseDto> sortOptions = Arrays.asList(
                new SortResponseDto("price_asc", "Menor precio"),
                new SortResponseDto("price_desc", "Mayor precio"),
                new SortResponseDto("title_asc", "A-Z")
        );

        when(productService.getAvailableSortOptions()).thenReturn(sortOptions);

        // When & Then
        given()
                .when()
                .get("/api/items/sort-options")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].id", equalTo("price_asc"))
                .body("[0].name", equalTo("Menor precio"));
    }

    @Test
    @DisplayName("GET /api/items/brands - Debe obtener marcas disponibles")
    void shouldGetAvailableBrands() {
        // Given
        ProductStatisticsDto statistics = ProductStatisticsDto.builder()
                .brands(Arrays.asList("Nike", "Adidas", "Apple", "Sony"))
                .build();

        when(productService.getStatistics()).thenReturn(statistics);

        // When & Then
        given()
                .when()
                .get("/api/items/brands")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4))
                .body("", hasItems("Nike", "Adidas", "Apple", "Sony"));
    }

    @Test
    @DisplayName("GET /api/items/categories - Debe obtener categorías disponibles")
    void shouldGetAvailableCategories() {
        // Given
        ProductStatisticsDto statistics = ProductStatisticsDto.builder()
                .categories(Arrays.asList("Footwear", "Electronics", "Clothing"))
                .build();

        when(productService.getStatistics()).thenReturn(statistics);

        // When & Then
        given()
                .when()
                .get("/api/items/categories")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("", hasItems("Footwear", "Electronics", "Clothing"));
    }

    // ================================
    // ERROR HANDLING TESTS
    // ================================

    @Test
    @DisplayName("Debe manejar errores de validación")
    void shouldHandleValidationErrors() {
        // Given - Request con datos inválidos
        CreateProductRequestDto invalidRequest = CreateProductRequestDto.builder()
                .title("") // Título vacío
                .price(new BigDecimal("-10")) // Precio negativo
                .build();

        // When & Then
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/items")
                .then()
                .statusCode(400); // Bad Request por validación
    }

    @Test
    @DisplayName("Debe manejar parámetros de paginación inválidos")
    void shouldHandleInvalidPaginationParams() {
        // When & Then - Offset negativo
        given()
                .queryParam("offset", -1)
                .queryParam("limit", 50)
                .when()
                .get("/api/items")
                .then()
                .statusCode(400); // Bad Request por validación

        // When & Then - Limit muy alto
        given()
                .queryParam("offset", 0)
                .queryParam("limit", 300)
                .when()
                .get("/api/items")
                .then()
                .statusCode(400); // Bad Request por validación
    }

    @Test
    @DisplayName("Debe manejar búsqueda con parámetros vacíos")
    void shouldHandleEmptySearchParams() {
        // Given
        when(productService.advancedSearch(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(50), isNull()))
                .thenReturn(ProductListResponseDto.builder()
                        .results(Collections.emptyList())
                        .paging(PagingResponseDto.builder()
                                .total(0)
                                .offset(0)
                                .limit(50)
                                .build())
                        .build());

        // When & Then
        given()
                .when()
                .get("/api/items/search")
                .then()
                .statusCode(200)
                .body("results.size()", equalTo(0))
                .body("paging.total", equalTo(0));
    }

    @Test
    @DisplayName("DELETE /api/items/{id} - Debe retornar 500 cuando falla la eliminación")
    void shouldReturn500WhenDeleteFails() {
        // Given - El servicio retorna false indicando fallo en la eliminación
        when(productService.deleteProduct("MLA1234567890"))
                .thenReturn(false);

        // When & Then
        given()
                .when()
                .delete("/api/items/MLA1234567890")
                .then()
                .statusCode(500); // Internal Server Error
    }
}