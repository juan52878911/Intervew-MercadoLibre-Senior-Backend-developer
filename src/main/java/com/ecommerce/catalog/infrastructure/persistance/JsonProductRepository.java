package com.ecommerce.catalog.infrastructure.persistance;

import com.ecommerce.catalog.application.dto.ProductDto;
import com.ecommerce.catalog.application.dto.ProductsContainerDto;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.TypeRef;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementación corregida del repositorio JSON
 * Maneja correctamente la deserialización y concurrencia
 */
@ApplicationScoped
@Slf4j
public class JsonProductRepository implements ProductRepository {

    private final List<ProductDto> products;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public JsonProductRepository() {
        // Configurar ObjectMapper para manejar snake_case del JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        try {
            // Cargar JSON al inicializar
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("data/products.json");

            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo products.json");
            }

            String jsonString = new String(inputStream.readAllBytes());

            // Configurar JsonPath
            Configuration config = Configuration.defaultConfiguration()
                    .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
                    .addOptions(Option.SUPPRESS_EXCEPTIONS);

            DocumentContext jsonContext = JsonPath.using(config).parse(jsonString);

            // Deserializar correctamente usando TypeRef para preservar tipos
            ProductsContainerDto container = objectMapper.readValue(jsonString, ProductsContainerDto.class);
            this.products = container.getProducts();

            log.info("✅ Repositorio JSON inicializado con {} productos", products.size());

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar productos del JSON", e);
        }
    }

    @Override
    public Optional<ProductDto> findById(String id) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando producto por ID: {}", id);

            // Usar stream de la lista en memoria (más confiable)
            Optional<ProductDto> result = products.stream()
                    .filter(product -> id.equals(product.getId()))
                    .findFirst();

            if (result.isPresent()) {
                log.debug("✅ Producto encontrado: {} - {}", result.get().getId(), result.get().getTitle());
            } else {
                log.debug("❌ Producto no encontrado: {}", id);
            }

            return result;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findAll() {
        lock.readLock().lock();
        try {
            log.debug("📋 Obteniendo todos los productos");
            return List.copyOf(products);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findByTitleContaining(String title) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos que contengan en título: '{}'", title);

            List<ProductDto> results = products.stream()
                    .filter(product -> product.getTitle() != null &&
                            product.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos con título que contiene: '{}'", results.size(), title);
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findByBrand(String brand) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos de marca: '{}'", brand);

            List<ProductDto> results = products.stream()
                    .filter(product -> product.getAttributes() != null &&
                            product.getAttributes().stream()
                                    .anyMatch(attr -> "BRAND".equals(attr.getId()) &&
                                            brand.equalsIgnoreCase(attr.getValueName())))
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos de marca: '{}'", results.size(), brand);
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos en rango de precio: {} - {}", minPrice, maxPrice);

            List<ProductDto> results = products.stream()
                    .filter(product -> product.getPrice() != null &&
                            product.getPrice().compareTo(minPrice) >= 0 &&
                            product.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos en rango de precio: {} - {}",
                    results.size(), minPrice, maxPrice);
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findByCondition(String condition) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos con condición: '{}'", condition);

            List<ProductDto> results = products.stream()
                    .filter(product -> condition.equalsIgnoreCase(product.getCondition()))
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos con condición: '{}'", results.size(), condition);
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findByStatus(String status) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos con estado: '{}'", status);

            List<ProductDto> results = products.stream()
                    .filter(product -> status.equalsIgnoreCase(product.getStatus()))
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos con estado: '{}'", results.size(), status);
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findByCurrency(String currencyId) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos con moneda: '{}'", currencyId);

            List<ProductDto> results = products.stream()
                    .filter(product -> currencyId.equalsIgnoreCase(product.getCurrencyId()))
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos con moneda: '{}'", results.size(), currencyId);
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> findWithVariations() {
        lock.readLock().lock();
        try {
            log.debug("🔍 Buscando productos que tienen variaciones");

            List<ProductDto> results = products.stream()
                    .filter(product -> product.getVariations() != null &&
                            !product.getVariations().isEmpty())
                    .collect(Collectors.toList());

            log.debug("✅ Encontrados {} productos con variaciones", results.size());
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ProductDto> searchAdvanced(String query, String brand, BigDecimal minPrice,
                                           BigDecimal maxPrice, String condition) {
        lock.readLock().lock();
        try {
            log.debug("🔍 Búsqueda avanzada - Query: '{}', Marca: '{}', Precio: {}-{}, Condición: '{}'",
                    query, brand, minPrice, maxPrice, condition);

            List<ProductDto> results = products.stream()
                    .filter(product -> {
                        // Filtro por query en título
                        if (query != null && !query.isBlank()) {
                            if (product.getTitle() == null ||
                                    !product.getTitle().toLowerCase().contains(query.toLowerCase())) {
                                return false;
                            }
                        }

                        // Filtro por marca
                        if (brand != null && !brand.isBlank()) {
                            if (product.getAttributes() == null) return false;
                            boolean hasBrand = product.getAttributes().stream()
                                    .anyMatch(attr -> "BRAND".equals(attr.getId()) &&
                                            brand.equalsIgnoreCase(attr.getValueName()));
                            if (!hasBrand) return false;
                        }

                        // Filtro por rango de precio
                        if (minPrice != null && (product.getPrice() == null ||
                                product.getPrice().compareTo(minPrice) < 0)) {
                            return false;
                        }
                        if (maxPrice != null && (product.getPrice() == null ||
                                product.getPrice().compareTo(maxPrice) > 0)) {
                            return false;
                        }

                        // Filtro por condición
                        if (condition != null && !condition.isBlank()) {
                            if (!condition.equalsIgnoreCase(product.getCondition())) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            log.debug("✅ Búsqueda avanzada completada. Encontrados {} productos", results.size());
            return results;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return products.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long countByBrand(String brand) {
        lock.readLock().lock();
        try {
            return findByBrand(brand).size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<String> findAllBrands() {
        lock.readLock().lock();
        try {
            log.debug("🔍 Obteniendo todas las marcas disponibles");

            List<String> brands = products.stream()
                    .filter(product -> product.getAttributes() != null)
                    .flatMap(product -> product.getAttributes().stream())
                    .filter(attr -> "BRAND".equals(attr.getId()) && attr.getValueName() != null)
                    .map(attr -> attr.getValueName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            log.debug("✅ Encontradas {} marcas únicas", brands.size());
            return brands;

        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<String> findAllCategories() {
        lock.readLock().lock();
        try {
            log.debug("🔍 Obteniendo todas las categorías disponibles");

            List<String> categories = products.stream()
                    .filter(product -> product.getAttributes() != null)
                    .flatMap(product -> product.getAttributes().stream())
                    .filter(attr -> ("FOOTWEAR_TYPE".equals(attr.getId()) ||
                            "CLOTHING_TYPE".equals(attr.getId()) ||
                            "MODEL".equals(attr.getId())) &&
                            attr.getValueName() != null)
                    .map(attr -> attr.getValueName())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            log.debug("✅ Encontradas {} categorías únicas", categories.size());
            return categories;

        } finally {
            lock.readLock().unlock();
        }
    }

    // Métodos para debugging
    public void printStatistics() {
        lock.readLock().lock();
        try {
            log.info("📊 ESTADÍSTICAS DEL REPOSITORIO JSON:");
            log.info("═══════════════════════════════════════");
            log.info("Total productos: {}", count());
            log.info("Productos activos: {}", findByStatus("active").size());
            log.info("Productos nuevos: {}", findByCondition("new").size());
            log.info("Productos con variaciones: {}", findWithVariations().size());
            log.info("Marcas disponibles: {}", String.join(", ", findAllBrands()));
            log.info("Categorías disponibles: {}", String.join(", ", findAllCategories()));
        } finally {
            lock.readLock().unlock();
        }
    }

    // Método para obtener productos raw (para debugging)
    public List<ProductDto> getRawProducts() {
        return List.copyOf(products);
    }
}