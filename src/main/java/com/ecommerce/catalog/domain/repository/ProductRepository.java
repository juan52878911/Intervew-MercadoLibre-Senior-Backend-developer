package com.ecommerce.catalog.domain.repository;

import com.ecommerce.catalog.application.dto.ProductDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz del repositorio de productos
 * Define operaciones tipo "base de datos" sobre los productos
 */
public interface ProductRepository {

    // Operaciones básicas CRUD
    Optional<ProductDto> findById(String id);
    List<ProductDto> findAll();
    long count();

    // Búsquedas por campos específicos
    List<ProductDto> findByTitleContaining(String title);
    List<ProductDto> findByBrand(String brand);
    List<ProductDto> findByCondition(String condition);
    List<ProductDto> findByStatus(String status);
    List<ProductDto> findByCurrency(String currencyId);

    // Búsquedas por rangos
    List<ProductDto> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    // Búsquedas especializadas
    List<ProductDto> findWithVariations();
    List<ProductDto> searchAdvanced(String query, String brand, BigDecimal minPrice,
                                    BigDecimal maxPrice, String condition);

    // Operaciones de agregación
    long countByBrand(String brand);
    List<String> findAllBrands();
    List<String> findAllCategories();
}