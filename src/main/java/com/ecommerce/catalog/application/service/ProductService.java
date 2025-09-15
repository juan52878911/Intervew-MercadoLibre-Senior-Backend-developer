package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.application.enums.ProductStatus;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import com.ecommerce.catalog.domain.exception.InvalidProductDataException;
import com.ecommerce.catalog.domain.exception.DuplicateProductException;
import com.ecommerce.catalog.infrastructure.web.dto.request.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de productos
 * Contiene toda la lógica de negocio para operaciones CRUD
 */
@ApplicationScoped
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final Validator validator;

    @Inject
    ProductService(ProductRepository productRepository, Validator validator) {
        this.productRepository = productRepository;
        this.validator = validator;
    }

    // ================================
    // OPERACIONES CREATE (C)
    // ================================

    /**
     * Crear un nuevo producto
     */
    public ProductDto createProduct(@Valid CreateProductRequestDto request) {
        log.info("🆕 Creando nuevo producto: {}", request.getTitle());

        // Validar request
        validateCreateRequest(request);

        // Generar ID único tipo MercadoLibre
        String productId = generateProductId();

        // Verificar que el ID no exista (aunque es muy improbable)
        if (productRepository.findById(productId).isPresent()) {
            throw new DuplicateProductException("El ID generado ya existe: " + productId);
        }

        // Mapear request a DTO
        ProductDto product = mapCreateRequestToDto(request, productId);

        // Aplicar reglas de negocio
        applyBusinessRulesForCreation(product);

        // Simular guardado (en implementación real se guardaría)
        log.info("✅ Producto creado exitosamente: {} - {}", product.getId(), product.getTitle());

        return product;
    }

    /**
     * Crear múltiples productos en batch
     */
    public List<ProductDto> createProducts(List<CreateProductRequestDto> requests) {
        log.info("🆕 Creando {} productos en batch", requests.size());

        if (requests.size() > 100) {
            throw new InvalidProductDataException("No se pueden crear más de 100 productos a la vez");
        }

        return requests.stream()
                .map(this::createProduct)
                .toList();
    }

    // ================================
    // OPERACIONES READ (R)
    // ================================

    /**
     * Obtener producto por ID
     */
    public ProductDto getProductById(String id) {
        log.debug("🔍 Buscando producto por ID: {}", id);

        validateProductId(id);

        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado: " + id));
    }

    /**
     * Obtener todos los productos con paginación
     */
    public ProductListResponseDto getAllProducts(int offset, int limit, String sortBy) {
        log.debug("📋 Obteniendo productos - Offset: {}, Limit: {}, Sort: {}", offset, limit, sortBy);

        validatePaginationParams(offset, limit);

        List<ProductDto> allProducts = productRepository.findAll();

        // Aplicar ordenamiento
        List<ProductDto> sortedProducts = applySorting(allProducts, sortBy);

        // Aplicar paginación
        List<ProductSummaryResponseDto> paginatedResults = applyPagination(sortedProducts, offset, limit);

        // Crear respuesta con metadatos
        PagingResponseDto paging = PagingResponseDto.builder()
                .total(allProducts.size())
                .offset(offset)
                .limit(limit)
                .build();

        return ProductListResponseDto.builder()
                .results(paginatedResults)
                .paging(paging)
                .build();
    }

    /**
     * Buscar productos por título
     */
    public List<ProductDto> searchByTitle(String title) {
        log.debug("🔍 Buscando productos por título: {}", title);

        if (title == null || title.trim().length() < 2) {
            throw new InvalidProductDataException("El título debe tener al menos 2 caracteres");
        }

        List<ProductDto> results = productRepository.findByTitleContaining(title.trim());

        log.debug("✅ Encontrados {} productos con título: {}", results.size(), title);
        return results;
    }

    /**
     * Buscar productos por marca
     */
    public List<ProductDto> searchByBrand(String brand) {
        log.debug("🔍 Buscando productos por marca: {}", brand);

        validateBrandExists(brand);

        List<ProductDto> results = productRepository.findByBrand(brand);

        log.debug("✅ Encontrados {} productos de marca: {}", results.size(), brand);
        return results;
    }

    /**
     * Buscar productos por rango de precio
     */
    public List<ProductDto> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, String currency) {
        log.debug("🔍 Buscando productos por precio: {} - {} {}", minPrice, maxPrice, currency);

        validatePriceRange(minPrice, maxPrice);

        List<ProductDto> results = productRepository.findByPriceRange(minPrice, maxPrice);

        // Filtrar por moneda si se especifica
        if (currency != null && !currency.isBlank()) {
            results = results.stream()
                    .filter(product -> currency.equalsIgnoreCase(product.getCurrencyId()))
                    .toList();
        }

        log.debug("✅ Encontrados {} productos en rango de precio", results.size());
        return results;
    }

    /**
     * Obtener opciones de ordenamiento disponibles
     */
    public List<SortResponseDto> getAvailableSortOptions() {
        return List.of(
                new SortResponseDto("relevance", "Más relevantes"),
                new SortResponseDto("price_asc", "Menor precio"),
                new SortResponseDto("price_desc", "Mayor precio"),
                new SortResponseDto("title_asc", "A-Z"),
                new SortResponseDto("title_desc", "Z-A"),
                new SortResponseDto("date_desc", "Más recientes"),
                new SortResponseDto("date_asc", "Más antiguos")
        );
    }

    /**
     * Crear respuesta de listado completa con metadatos
     */
    private ProductListResponseDto createListResponse(List<ProductSummaryResponseDto> results,
                                                   String query, int offset, int limit,
                                                   int total, String sortBy) {
        // Crear paginación
        PagingResponseDto paging = new PagingResponseDto(total, offset, limit);

        // Crear opciones de ordenamiento
        List<SortResponseDto> availableSorts = getAvailableSortOptions();

        // Marcar el ordenamiento activo
        SortResponseDto currentSort = null;
        if (sortBy != null && !sortBy.isBlank()) {
            currentSort = availableSorts.stream()
                    .filter(sort -> sortBy.equals(sort.getId()))
                    .findFirst()
                    .map(sort -> {
                        sort.setActive(true);
                        return sort;
                    })
                    .orElse(null);
        }

        return ProductListResponseDto.builder()
                .siteId("MLA") // Simulando site de Argentina
                .query(query)
                .results(results)
                .paging(paging)
                .sort(currentSort)
                .availableSorts(availableSorts)
                .build();
    }

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    public ProductListResponseDto advancedSearch(String query, String brand, BigDecimal minPrice,
                                              BigDecimal maxPrice, String condition,
                                              int offset, int limit, String sortBy) {
        log.info("🔍 Búsqueda avanzada - Query: '{}', Marca: '{}', Precio: {}-{}, Condición: '{}', Sort: '{}'",
                query, brand, minPrice, maxPrice, condition, sortBy);

        validatePaginationParams(offset, limit);
        if (minPrice != null && maxPrice != null) {
            validatePriceRange(minPrice, maxPrice);
        }

        List<ProductDto> results = productRepository.searchAdvanced(query, brand, minPrice, maxPrice, condition);

        // Aplicar ordenamiento
        List<ProductDto> sortedResults = applySorting(results, sortBy);

        // Aplicar paginación
        List<ProductSummaryResponseDto> paginatedResults = applyPagination(sortedResults, offset, limit);

        // Crear respuesta completa
        ProductListResponseDto response = createListResponse(paginatedResults, query, offset, limit, results.size(), sortBy);

        log.info("✅ Búsqueda avanzada completada. {} resultados encontrados", results.size());
        return response;
    }

    // ================================
    // OPERACIONES UPDATE (U)
    // ================================

    /**
     * Actualizar producto completo
     */
    public ProductDto updateProduct(String id, @Valid UpdateProductRequestDto request) {
        log.info("🔄 Actualizando producto: {}", id);

        // Verificar que el producto existe
        ProductDto existingProduct = getProductById(id);

        // Validar request
        validateUpdateRequest(request);

        // Aplicar cambios
        ProductDto updatedProduct = applyUpdates(existingProduct, request);

        // Simular guardado
        log.info("✅ Producto actualizado exitosamente: {}", id);

        return updatedProduct;
    }

    /**
     * Actualizar precio específico
     */
    public ProductDto updatePrice(String id, BigDecimal newPrice, String reason) {
        log.info("💰 Actualizando precio del producto {} a {}", id, newPrice);

        ProductDto product = getProductById(id);

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("El precio debe ser mayor a 0");
        }

        BigDecimal oldPrice = product.getPrice();
        product.setPrice(newPrice);
        product.setLastUpdated(LocalDateTime.now());

        // Log del cambio de precio
        log.info("💰 Precio actualizado: {} -> {} (Razón: {})", oldPrice, newPrice, reason);

        return product;
    }

    /**
     * Actualizar estado del producto
     */
    public ProductDto updateStatus(String id, String newStatus) {
        log.info("📝 Actualizando estado del producto {} a {}", id, newStatus);

        ProductDto product = getProductById(id);

        validateStatus(newStatus);
        validateStatusTransition(product.getStatus(), newStatus);

        String oldStatus = product.getStatus();
        product.setStatus(newStatus);
        product.setLastUpdated(LocalDateTime.now());

        log.info("📝 Estado actualizado: {} -> {}", oldStatus, newStatus);

        return product;
    }

    // ================================
    // OPERACIONES DELETE (D)
    // ================================

    /**
     * Eliminar producto (soft delete)
     */
    public boolean deleteProduct(String id) {
        log.info("🗑️ Eliminando producto: {}", id);

        ProductDto product = getProductById(id);

        // Verificar que se puede eliminar
        validateCanDelete(product);

        // Soft delete: cambiar estado a "closed"
        product.setStatus(ProductStatus.CLOSED.getValue());
        product.setLastUpdated(LocalDateTime.now());

        log.info("✅ Producto eliminado (soft delete): {}", id);
        return true;
    }

    /**
     * Eliminar productos en batch
     */
    public BatchOperationResultDto deleteProducts(List<String> ids) {
        log.info("🗑️ Eliminando {} productos en batch", ids.size());

        int successful = 0;
        int failed = 0;

        for (String id : ids) {
            try {
                deleteProduct(id);
                successful++;
            } catch (Exception e) {
                log.error("Error al eliminar producto {}: {}", id, e.getMessage());
                failed++;
            }
        }

        return BatchOperationResultDto.builder()
                .totalProcessed(ids.size())
                .successful(successful)
                .failed(failed)
                .build();
    }

    // ================================
    // OPERACIONES DE ANÁLISIS
    // ================================

    /**
     * Obtener estadísticas de productos
     */
    public ProductStatisticsDto getStatistics() {
        log.debug("📊 Generando estadísticas de productos");

        long totalProducts = productRepository.count();
        List<String> brands = productRepository.findAllBrands();
        List<String> categories = productRepository.findAllCategories();
        long activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE.getValue()).size();
        long productsWithVariations = productRepository.findWithVariations().size();

        return ProductStatisticsDto.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalBrands(brands.size())
                .totalCategories(categories.size())
                .productsWithVariations(productsWithVariations)
                .brands(brands)
                .categories(categories)
                .build();
    }

    // ================================
    // MÉTODOS AUXILIARES
    // ================================

    private void validateCreateRequest(CreateProductRequestDto request) {
        Set<ConstraintViolation<CreateProductRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new InvalidProductDataException("Errores de validación: " + errors);
        }
    }

    private void validateUpdateRequest(UpdateProductRequestDto request) {
        Set<ConstraintViolation<UpdateProductRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new InvalidProductDataException("Errores de validación: " + errors);
        }
    }

    private void validateProductId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidProductDataException("El ID del producto no puede estar vacío");
        }
        if (!id.matches("^MLA\\d+$")) {
            throw new InvalidProductDataException("El ID debe tener formato MLA seguido de números");
        }
    }

    private void validatePaginationParams(int offset, int limit) {
        if (offset < 0) {
            throw new InvalidProductDataException("El offset no puede ser negativo");
        }
        if (limit <= 0 || limit > 200) {
            throw new InvalidProductDataException("El limit debe estar entre 1 y 200");
        }
    }

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("El precio mínimo no puede ser negativo");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("El precio máximo debe ser mayor a 0");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidProductDataException("El precio mínimo no puede ser mayor al máximo");
        }
    }

    private void validateBrandExists(String brand) {
        List<String> availableBrands = productRepository.findAllBrands();
        if (!availableBrands.contains(brand)) {
            throw new InvalidProductDataException("La marca '" + brand + "' no existe. Marcas disponibles: " +
                    String.join(", ", availableBrands));
        }
    }

    private void validateStatus(String status) {
        List<String> validStatuses = Arrays.stream(ProductStatus.values())
                .map(ProductStatus::getValue)
                .toList();
        if (!validStatuses.contains(status)) {
            throw new InvalidProductDataException("Estado inválido. Estados válidos: " +
                    String.join(", ", validStatuses));
        }
    }

    private void validateStatusTransition(String fromStatus, String toStatus) {
        // Reglas de transición de estado
        if ("closed".equals(fromStatus)) {
            throw new InvalidProductDataException("No se puede cambiar el estado de un producto cerrado");
        }
    }

    private void validateCanDelete(ProductDto product) {
        if ("closed".equals(product.getStatus())) {
            throw new InvalidProductDataException("El producto ya está eliminado");
        }
    }

    private String generateProductId() {
        // Generar ID único tipo MercadoLibre
        return "MLA" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
    }

    private ProductDto mapCreateRequestToDto(CreateProductRequestDto request, String id) {
        return ProductDto.builder()
                .id(id)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .currencyId(request.getCurrencyId())
                .condition(request.getCondition())
                .status("active") // Estado inicial
                .thumbnail(request.getThumbnail())
                .dateCreated(LocalDateTime.now())
                .lastUpdated(LocalDateTime.now())
                // Mapear pictures, attributes, variations si es necesario
                .build();
    }

    private void applyBusinessRulesForCreation(ProductDto product) {
        // Reglas de negocio para creación
        // Ejemplo: productos nuevos deben tener precio mínimo
        if ("new".equals(product.getCondition()) &&
                product.getPrice().compareTo(new BigDecimal("100")) < 0) {
            throw new InvalidProductDataException("Productos nuevos deben tener precio mínimo de $100");
        }
    }

    private ProductDto applyUpdates(ProductDto existing, UpdateProductRequestDto request) {
        // Aplicar solo los campos que no son null en el request
        if (request.getTitle() != null) existing.setTitle(request.getTitle());
        if (request.getDescription() != null) existing.setDescription(request.getDescription());
        if (request.getPrice() != null) existing.setPrice(request.getPrice());
        if (request.getCurrencyId() != null) existing.setCurrencyId(request.getCurrencyId());
        if (request.getCondition() != null) existing.setCondition(request.getCondition());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getThumbnail() != null) existing.setThumbnail(request.getThumbnail());

        existing.setLastUpdated(LocalDateTime.now());

        return existing;
    }

    private List<ProductDto> applySorting(List<ProductDto> products, String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return products;
        }

        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> products.stream()
                    .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                    .toList();
            case "price_desc" -> products.stream()
                    .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                    .toList();
            case "title_asc" -> products.stream()
                    .sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))
                    .toList();
            case "date_desc" -> products.stream()
                    .sorted((a, b) -> b.getDateCreated().compareTo(a.getDateCreated()))
                    .toList();
            default -> products;
        };
    }

    private List<ProductSummaryResponseDto> applyPagination(List<ProductDto> products, int offset, int limit) {
        return products.stream()
                .skip(offset)
                .limit(limit)
                .map(this::toSummaryResponse)
                .toList();
    }

    private ProductSummaryResponseDto toSummaryResponse(ProductDto product) {
        return ProductSummaryResponseDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .currencyId(product.getCurrencyId())
                .condition(product.getCondition())
                .thumbnail(product.getThumbnail())
                .status(product.getStatus())
                .build();
    }
}