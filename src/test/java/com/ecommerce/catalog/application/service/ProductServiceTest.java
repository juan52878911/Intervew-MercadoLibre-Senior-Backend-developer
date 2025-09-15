package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.catalog.domain.exception.*;
import com.ecommerce.catalog.infrastructure.web.dto.request.CreateProductRequestDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.ProductListResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.SortResponseDto;
import com.ecommerce.catalog.infrastructure.web.dto.request.UpdateProductRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Complete Product Service Tests")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Validator validator;

    private ProductService productService;

    private ProductDto sampleProduct;
    private CreateProductRequestDto createRequest;
    private UpdateProductRequestDto updateRequest;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, validator);

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
    }

    // ================================
    // TESTS CREATE OPERATIONS
    // ================================

    @Test
    @DisplayName("Debe crear producto exitosamente")
    void shouldCreateProductSuccessfully() {
        // Given
        when(validator.validate(any(CreateProductRequestDto.class))).thenReturn(Collections.emptySet());
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        ProductDto result = productService.createProduct(createRequest);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId().startsWith("MLA"));
        assertEquals(createRequest.getTitle(), result.getTitle());
        assertEquals(createRequest.getDescription(), result.getDescription());
        assertEquals(createRequest.getPrice(), result.getPrice());
        assertEquals("active", result.getStatus());
        assertNotNull(result.getDateCreated());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    @DisplayName("Debe fallar al crear producto con ID duplicado")
    void shouldFailToCreateProductWithDuplicateId() {
        // Given
        when(validator.validate(any(CreateProductRequestDto.class))).thenReturn(Collections.emptySet());
        when(productRepository.findById(anyString())).thenReturn(Optional.of(sampleProduct));

        // When & Then
        assertThrows(DuplicateProductException.class, () -> {
            productService.createProduct(createRequest);
        });
    }

    @Test
    @DisplayName("Debe crear múltiples productos en batch")
    void shouldCreateMultipleProductsInBatch() {
        // Given
        List<CreateProductRequestDto> requests = Arrays.asList(createRequest, createRequest);
        when(validator.validate(any(CreateProductRequestDto.class))).thenReturn(Collections.emptySet());
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // When
        List<ProductDto> results = productService.createProducts(requests);

        // Then
        assertEquals(2, results.size());
        results.forEach(product -> {
            assertNotNull(product.getId());
            assertTrue(product.getId().startsWith("MLA"));
        });
    }

    @Test
    @DisplayName("Debe fallar al crear más de 100 productos en batch")
    void shouldFailToCreateMoreThan100ProductsInBatch() {
        // Given
        List<CreateProductRequestDto> requests = Collections.nCopies(101, createRequest);

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.createProducts(requests);
        });
    }

    @Test
    @DisplayName("Debe aplicar reglas de negocio para productos nuevos con precio bajo")
    void shouldApplyBusinessRulesForNewProductsWithLowPrice() {
        // Given
        CreateProductRequestDto lowPriceRequest = CreateProductRequestDto.builder()
                .title("Cheap Product")
                .description("Very cheap product")
                .price(new BigDecimal("50.00")) // Menos del mínimo
                .currencyId("ARS")
                .condition("new")
                .thumbnail("https://example.com/image.jpg")
                .build();

        when(validator.validate(any(CreateProductRequestDto.class))).thenReturn(Collections.emptySet());
        when(productRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.createProduct(lowPriceRequest);
        });
    }

    // ================================
    // TESTS READ OPERATIONS
    // ================================

    @Test
    @DisplayName("Debe obtener producto por ID exitosamente")
    void shouldGetProductByIdSuccessfully() {
        // Given
        String productId = "MLA1234567890";
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // When
        ProductDto result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals(sampleProduct.getTitle(), result.getTitle());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando producto no existe")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        String productId = "MLA9999999999";
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(productId);
        });
    }

    @Test
    @DisplayName("Debe validar formato de ID de producto")
    void shouldValidateProductIdFormat() {
        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.getProductById("INVALID_ID");
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.getProductById("");
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.getProductById(null);
        });
    }

    @Test
    @DisplayName("Debe obtener todos los productos con paginación")
    void shouldGetAllProductsWithPagination() {
        // Given
        List<ProductDto> allProducts = Arrays.asList(sampleProduct, sampleProduct);
        when(productRepository.findAll()).thenReturn(allProducts);

        // When
        ProductListResponseDto result = productService.getAllProducts(0, 10, null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
        assertNotNull(result.getPaging());
        assertEquals(2, result.getPaging().getTotal());
        assertEquals(0, result.getPaging().getOffset());
        assertEquals(10, result.getPaging().getLimit());
    }

    @Test
    @DisplayName("Debe buscar productos por título")
    void shouldSearchProductsByTitle() {
        // Given
        String title = "Test";
        List<ProductDto> expectedProducts = Collections.singletonList(sampleProduct);
        when(productRepository.findByTitleContaining(title)).thenReturn(expectedProducts);

        // When
        List<ProductDto> results = productService.searchByTitle(title);

        // Then
        assertEquals(1, results.size());
        assertEquals(sampleProduct.getId(), results.getFirst().getId());
        verify(productRepository).findByTitleContaining(title);
    }

    @Test
    @DisplayName("Debe validar título mínimo en búsqueda")
    void shouldValidateMinimumTitleInSearch() {
        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.searchByTitle("A"); // Muy corto
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.searchByTitle(null);
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.searchByTitle("  "); // Solo espacios
        });
    }

    @Test
    @DisplayName("Debe buscar productos por marca")
    void shouldSearchProductsByBrand() {
        // Given
        String brand = "Nike";
        List<String> availableBrands = Arrays.asList("Nike", "Adidas", "Apple");
        List<ProductDto> expectedProducts = Collections.singletonList(sampleProduct);

        when(productRepository.findAllBrands()).thenReturn(availableBrands);
        when(productRepository.findByBrand(brand)).thenReturn(expectedProducts);

        // When
        List<ProductDto> results = productService.searchByBrand(brand);

        // Then
        assertEquals(1, results.size());
        assertEquals(sampleProduct.getId(), results.getFirst().getId());
        verify(productRepository).findByBrand(brand);
    }

    @Test
    @DisplayName("Debe fallar al buscar marca inexistente")
    void shouldFailWhenSearchingNonExistentBrand() {
        // Given
        String brand = "NonExistentBrand";
        List<String> availableBrands = Arrays.asList("Nike", "Adidas", "Apple");
        when(productRepository.findAllBrands()).thenReturn(availableBrands);

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.searchByBrand(brand);
        });
    }

    @Test
    @DisplayName("Debe buscar productos por rango de precio")
    void shouldSearchProductsByPriceRange() {
        // Given
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("200.00");
        String currency = "ARS";
        List<ProductDto> expectedProducts = Collections.singletonList(sampleProduct);

        when(productRepository.findByPriceRange(minPrice, maxPrice)).thenReturn(expectedProducts);

        // When
        List<ProductDto> results = productService.searchByPriceRange(minPrice, maxPrice, currency);

        // Then
        assertEquals(1, results.size());
        verify(productRepository).findByPriceRange(minPrice, maxPrice);
    }

    @Test
    @DisplayName("Debe validar rango de precios")
    void shouldValidatePriceRange() {

        BigDecimal minPrice = new BigDecimal("100.00");
        BigDecimal maxPrice = new BigDecimal("200.00");

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.searchByPriceRange(
                    maxPrice,
                    minPrice,
                    "ARS"
            ); // Min > Max
        });

        BigDecimal arange = new BigDecimal("-10.00");
        BigDecimal brange = new BigDecimal("100.00");

        assertThrows(InvalidProductDataException.class, () -> {
            productService.searchByPriceRange(
                    arange,
                    brange,
                    "ARS"
            ); // Precio negativo
        });
    }

    @Test
    @DisplayName("Debe realizar búsqueda avanzada")
    void shouldPerformAdvancedSearch() {
        // Given
        String query = "Test";
        String brand = "Nike";
        BigDecimal minPrice = new BigDecimal("50.00");
        BigDecimal maxPrice = new BigDecimal("200.00");
        String condition = "new";
        int offset = 0;
        int limit = 10;
        String sortBy = "price_asc";

        List<ProductDto> expectedProducts = Collections.singletonList(sampleProduct);
        when(productRepository.searchAdvanced(query, brand, minPrice, maxPrice, condition))
                .thenReturn(expectedProducts);

        // When
        ProductListResponseDto result = productService.advancedSearch(
                query, brand, minPrice, maxPrice, condition, offset, limit, sortBy);

        // Then
        assertNotNull(result);
        assertEquals(query, result.getQuery());
        assertEquals(1, result.getResults().size());
        assertNotNull(result.getPaging());
        assertEquals(1, result.getPaging().getTotal());
        assertEquals("MLA", result.getSiteId());
    }

    @Test
    @DisplayName("Debe obtener opciones de ordenamiento")
    void shouldGetAvailableSortOptions() {
        // When
        List<SortResponseDto> sortOptions = productService.getAvailableSortOptions();

        // Then
        assertNotNull(sortOptions);
        assertFalse(sortOptions.isEmpty());
        assertTrue(sortOptions.size() >= 5);

        // Verificar opciones específicas
        assertTrue(sortOptions.stream().anyMatch(s -> "price_asc".equals(s.getId())));
        assertTrue(sortOptions.stream().anyMatch(s -> "price_desc".equals(s.getId())));
        assertTrue(sortOptions.stream().anyMatch(s -> "title_asc".equals(s.getId())));
    }

    @Test
    @DisplayName("Debe validar parámetros de paginación")
    void shouldValidatePaginationParams() {
        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.getAllProducts(-1, 10, null); // Offset negativo
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.getAllProducts(0, 0, null); // Limit cero
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.getAllProducts(0, 300, null); // Limit muy alto
        });
    }

    // ================================
    // TESTS UPDATE OPERATIONS
    // ================================

    @Test
    @DisplayName("Debe actualizar producto exitosamente")
    void shouldUpdateProductSuccessfully() {
        // Given
        String productId = "MLA1234567890";
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
        when(validator.validate(any(UpdateProductRequestDto.class))).thenReturn(Collections.emptySet());

        // When
        ProductDto result = productService.updateProduct(productId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getTitle(), result.getTitle());
        assertEquals(updateRequest.getPrice(), result.getPrice());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    @DisplayName("Debe actualizar precio específico")
    void shouldUpdateSpecificPrice() {
        // Given
        String productId = "MLA1234567890";
        BigDecimal newPrice = new BigDecimal("180.00");
        String reason = "Promoción especial";

        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // When
        ProductDto result = productService.updatePrice(productId, newPrice, reason);

        // Then
        assertNotNull(result);
        assertEquals(newPrice, result.getPrice());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    @DisplayName("Debe fallar al actualizar precio inválido")
    void shouldFailToUpdateInvalidPrice() {
        // Given
        String productId = "MLA1234567890";
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        BigDecimal bigTen = new BigDecimal("-10.00");
        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.updatePrice(productId, bigTen, "Test");
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.updatePrice(productId, BigDecimal.ZERO, "Test");
        });

        assertThrows(InvalidProductDataException.class, () -> {
            productService.updatePrice(productId, null, "Test");
        });
    }

    @Test
    @DisplayName("Debe actualizar estado del producto")
    void shouldUpdateProductStatus() {
        // Given
        String productId = "MLA1234567890";
        String newStatus = "paused";

        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // When
        ProductDto result = productService.updateStatus(productId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    @DisplayName("Debe validar estados válidos")
    void shouldValidateValidStatuses() {
        // Given
        String productId = "MLA1234567890";
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.updateStatus(productId, "invalid_status");
        });
    }

    @Test
    @DisplayName("Debe aplicar reglas de transición de estado")
    void shouldApplyStatusTransitionRules() {
        // Given
        String productId = "MLA1234567890";
        ProductDto closedProduct = ProductDto.builder()
                .id(productId)
                .title("Closed Product")
                .status("closed")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(closedProduct));

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.updateStatus(productId, "active");
        });
    }

    // ================================
    // TESTS DELETE OPERATIONS
    // ================================

    @Test
    @DisplayName("Debe eliminar producto exitosamente (soft delete)")
    void shouldDeleteProductSuccessfully() {
        // Given
        String productId = "MLA1234567890";
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // When
        boolean result = productService.deleteProduct(productId);

        // Then
        assertTrue(result);
        assertEquals("closed", sampleProduct.getStatus()); // Soft delete
    }

    @Test
    @DisplayName("Debe fallar al eliminar producto ya cerrado")
    void shouldFailToDeleteAlreadyClosedProduct() {
        // Given
        String productId = "MLA1234567890";
        sampleProduct.setStatus("closed");
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        // When & Then
        assertThrows(InvalidProductDataException.class, () -> {
            productService.deleteProduct(productId);
        });
    }

    @Test
    @DisplayName("Debe eliminar productos en batch")
    void shouldDeleteProductsInBatch() {
        // Given
        List<String> ids = Arrays.asList("MLA1136716168", "MLA1234567890");

        // Mock que crea una nueva instancia para cada llamada
        when(productRepository.findById(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return Optional.of(ProductDto.builder()
                    .id(id)
                    .title("Test Product " + id)
                    .status("active")
                    .build());
        });

        // When
        BatchOperationResultDto result = productService.deleteProducts(ids);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalProcessed());
        assertEquals(2, result.getSuccessful());
        assertEquals(0, result.getFailed());
    }

    @Test
    @DisplayName("Debe manejar errores en eliminación batch")
    void shouldHandleErrorsInBatchDeletion() {
        // Given
        List<String> ids = Arrays.asList("MLA1234567890", "MLA9999999999");
        when(productRepository.findById("MLA1234567890")).thenReturn(Optional.of(sampleProduct));
        when(productRepository.findById("MLA9999999999")).thenReturn(Optional.empty());

        // When
        BatchOperationResultDto result = productService.deleteProducts(ids);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalProcessed());
        assertEquals(1, result.getSuccessful());
        assertEquals(1, result.getFailed());
    }

    // ================================
    // TESTS STATISTICS
    // ================================

    @Test
    @DisplayName("Debe generar estadísticas correctamente")
    void shouldGenerateStatisticsCorrectly() {
        // Given
        List<String> brands = Arrays.asList("Nike", "Adidas", "Apple");
        List<String> categories = Arrays.asList("Footwear", "Electronics", "Clothing");
        List<ProductDto> activeProducts = Collections.singletonList(sampleProduct);
        List<ProductDto> productsWithVariations = Collections.singletonList(sampleProduct);

        when(productRepository.count()).thenReturn(10L);
        when(productRepository.findAllBrands()).thenReturn(brands);
        when(productRepository.findAllCategories()).thenReturn(categories);
        when(productRepository.findByStatus("active")).thenReturn(activeProducts);
        when(productRepository.findWithVariations()).thenReturn(productsWithVariations);

        // When
        ProductStatisticsDto result = productService.getStatistics();

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getTotalProducts());
        assertEquals(1L, result.getActiveProducts());
        assertEquals(3L, result.getTotalBrands());
        assertEquals(3L, result.getTotalCategories());
        assertEquals(1L, result.getProductsWithVariations());
        assertEquals(brands, result.getBrands());
        assertEquals(categories, result.getCategories());
    }

    // ================================
    // TESTS SORTING AND PAGINATION
    // ================================

    @Test
    @DisplayName("Debe aplicar ordenamiento por precio ascendente")
    void shouldApplyPriceAscendingSorting() {
        // Given
        ProductDto product1 = ProductDto.builder()
                .id("MLA1")
                .title("Product A")
                .price(new BigDecimal("200.00"))
                .dateCreated(LocalDateTime.now())
                .build();

        ProductDto product2 = ProductDto.builder()
                .id("MLA2")
                .title("Product B")
                .price(new BigDecimal("100.00"))
                .dateCreated(LocalDateTime.now())
                .build();

        List<ProductDto> products = Arrays.asList(product1, product2); // Desordenados
        when(productRepository.findAll()).thenReturn(products);

        // When
        ProductListResponseDto result = productService.getAllProducts(0, 10, "price_asc");

        // Then
        assertEquals(2, result.getResults().size());
        // El primer resultado debe ser el más barato
        assertEquals("MLA2", result.getResults().getFirst().getId());
        assertEquals(new BigDecimal("100.00"), result.getResults().getFirst().getPrice());
    }

    @Test
    @DisplayName("Debe aplicar ordenamiento por título")
    void shouldApplyTitleSorting() {
        // Given
        ProductDto productZ = ProductDto.builder()
                .id("MLA1")
                .title("Z Product")
                .price(new BigDecimal("100.00"))
                .dateCreated(LocalDateTime.now())
                .build();

        ProductDto productA = ProductDto.builder()
                .id("MLA2")
                .title("A Product")
                .price(new BigDecimal("100.00"))
                .dateCreated(LocalDateTime.now())
                .build();

        List<ProductDto> products = Arrays.asList(productZ, productA);
        when(productRepository.findAll()).thenReturn(products);

        // When
        ProductListResponseDto result = productService.getAllProducts(0, 10, "title_asc");

        // Then
        assertEquals(2, result.getResults().size());
        assertEquals("MLA2", result.getResults().getFirst().getId()); // A Product primero
    }

    @Test
    @DisplayName("Debe manejar lista vacía correctamente")
    void shouldHandleEmptyListCorrectly() {
        // Given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ProductListResponseDto result = productService.getAllProducts(0, 10, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getResults().isEmpty());
        assertEquals(0, result.getPaging().getTotal());
    }

    @Test
    @DisplayName("Debe aplicar paginación correctamente")
    void shouldApplyPaginationCorrectly() {
        // Given
        List<ProductDto> products = Arrays.asList(
                sampleProduct, sampleProduct, sampleProduct, sampleProduct, sampleProduct
        );
        when(productRepository.findAll()).thenReturn(products);

        // When
        ProductListResponseDto result = productService.getAllProducts(1, 2, null); // Skip 1, take 2

        // Then
        assertEquals(2, result.getResults().size());
        assertEquals(5, result.getPaging().getTotal());
        assertEquals(1, result.getPaging().getOffset());
        assertEquals(2, result.getPaging().getLimit());
    }

    // ================================
    // TESTS VALIDATION ERRORS
    // ================================

    @Test
    @DisplayName("Debe manejar errores de validación en creación")
    void shouldHandleValidationErrorsInCreation() {
        // Given
        Set<ConstraintViolation<CreateProductRequestDto>> violations = new HashSet<>();
        ConstraintViolation<CreateProductRequestDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Campo obligatorio");
        violations.add(violation);

        when(validator.validate(any(CreateProductRequestDto.class))).thenReturn(violations);

        // When & Then
        InvalidProductDataException exception = assertThrows(InvalidProductDataException.class, () -> {
            productService.createProduct(createRequest);
        });

        assertTrue(exception.getMessage().contains("Errores de validación"));
        assertTrue(exception.getMessage().contains("Campo obligatorio"));
    }

    @Test
    @DisplayName("Debe manejar errores de validación en actualización")
    void shouldHandleValidationErrorsInUpdate() {
        // Given
        String productId = "MLA1234567890";
        when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

        Set<ConstraintViolation<UpdateProductRequestDto>> violations = new HashSet<>();
        ConstraintViolation<UpdateProductRequestDto> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Precio inválido");
        violations.add(violation);

        when(validator.validate(any(UpdateProductRequestDto.class))).thenReturn(violations);

        // When & Then
        InvalidProductDataException exception = assertThrows(InvalidProductDataException.class, () -> {
            productService.updateProduct(productId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("Errores de validación"));
        assertTrue(exception.getMessage().contains("Precio inválido"));
    }
}