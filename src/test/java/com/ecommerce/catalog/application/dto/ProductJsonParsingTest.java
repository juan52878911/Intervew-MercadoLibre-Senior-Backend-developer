package com.ecommerce.catalog.application.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Product JSON Parsing Tests")
class ProductJsonParsingTest {

    private ProductsContainerDto productsContainer;

    @BeforeEach
    void setUp() throws IOException {
        // Configurar ObjectMapper con soporte para LocalDateTime
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Cargar el archivo JSON desde resources
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("data/products.json");

        assertNotNull(inputStream, "El archivo products.json debe existir en src/main/resources/data/");

        // Deserializar el JSON
        productsContainer = objectMapper.readValue(inputStream, ProductsContainerDto.class);
    }

    @Test
    @DisplayName("Debe cargar el contenedor de productos correctamente")
    void shouldLoadProductsContainer() {
        assertNotNull(productsContainer, "El contenedor de productos no debe ser null");
        assertNotNull(productsContainer.getProducts(), "La lista de productos no debe ser null");
        assertFalse(productsContainer.getProducts().isEmpty(), "Debe haber al menos un producto");

        System.out.println("✅ Productos cargados: " + productsContainer.getProducts().size());
    }

    @Test
    @DisplayName("Debe validar que todos los productos tienen campos obligatorios")
    void shouldValidateRequiredFields() {
        List<ProductDto> products = productsContainer.getProducts();

        for (ProductDto product : products) {
            // Validar campos obligatorios
            assertNotNull(product.getId(), "Product ID no debe ser null");
            assertNotNull(product.getTitle(), "Product title no debe ser null");
            assertNotNull(product.getDescription(), "Product description no debe ser null");
            assertNotNull(product.getPrice(), "Product price no debe ser null");
            assertNotNull(product.getCurrencyId(), "Product currencyId no debe ser null");
            assertNotNull(product.getCondition(), "Product condition no debe ser null");
            assertNotNull(product.getStatus(), "Product status no debe ser null");

            // Validar que los campos no estén vacíos
            assertFalse(product.getId().isBlank(), "Product ID no debe estar vacío");
            assertFalse(product.getTitle().isBlank(), "Product title no debe estar vacío");
            assertFalse(product.getDescription().isBlank(), "Product description no debe estar vacío");

            // Validar precio positivo
            assertTrue(product.getPrice().compareTo(BigDecimal.ZERO) > 0,
                    "El precio debe ser mayor a 0 para el producto: " + product.getId());

            System.out.println("✅ Producto válido: " + product.getId() + " - " + product.getTitle());
        }
    }

    @Test
    @DisplayName("Debe validar productos específicos del JSON")
    void shouldValidateSpecificProducts() {
        List<ProductDto> products = productsContainer.getProducts();

        // Buscar el producto de Nike
        ProductDto nikeProduct = products.stream()
                .filter(p -> p.getId().equals("MLA1136716168"))
                .findFirst()
                .orElse(null);

        assertNotNull(nikeProduct, "Debe existir el producto Nike con ID MLA1136716168");
        assertEquals("Zapatillas Nike Air Max 270 - Negras", nikeProduct.getTitle());
        assertEquals(new BigDecimal("89999.99"), nikeProduct.getPrice());
        assertEquals("ARS", nikeProduct.getCurrencyId());
        assertEquals("new", nikeProduct.getCondition());
        assertEquals("active", nikeProduct.getStatus());

        // Buscar el producto iPhone
        ProductDto iphoneProduct = products.stream()
                .filter(p -> p.getId().equals("MLA2234567890"))
                .findFirst()
                .orElse(null);

        assertNotNull(iphoneProduct, "Debe existir el producto iPhone con ID MLA2234567890");
        assertEquals("iPhone 15 Pro 128GB Titanio Natural", iphoneProduct.getTitle());
        assertEquals(new BigDecimal("1299999.00"), iphoneProduct.getPrice());
        assertTrue(iphoneProduct.getDescription().contains("A17 Pro"));

        System.out.println("✅ Productos específicos validados correctamente");
    }

    @Test
    @DisplayName("Debe validar las imágenes de los productos")
    void shouldValidateProductPictures() {
        List<ProductDto> products = productsContainer.getProducts();

        for (ProductDto product : products) {
            if (product.getPictures() != null && !product.getPictures().isEmpty()) {
                for (PictureDto picture : product.getPictures()) {
                    assertNotNull(picture.getId(), "Picture ID no debe ser null");
                    assertNotNull(picture.getUrl(), "Picture URL no debe ser null");
                    assertNotNull(picture.getSecureUrl(), "Picture secure URL no debe ser null");

                    // Validar formato de URLs
                    assertTrue(picture.getUrl().startsWith("http"),
                            "URL debe comenzar con http para el producto: " + product.getId());
                    assertTrue(picture.getSecureUrl().startsWith("https"),
                            "Secure URL debe comenzar con https para el producto: " + product.getId());
                }
            }
        }

        System.out.println("✅ Imágenes validadas correctamente");
    }

    @Test
    @DisplayName("Debe validar los atributos de los productos")
    void shouldValidateProductAttributes() {
        List<ProductDto> products = productsContainer.getProducts();

        for (ProductDto product : products) {
            if (product.getAttributes() != null && !product.getAttributes().isEmpty()) {

                // Verificar que cada producto tenga al menos un atributo de BRAND
                boolean hasBrand = product.getAttributes().stream()
                        .anyMatch(attr -> "BRAND".equals(attr.getId()));
                assertTrue(hasBrand, "El producto " + product.getId() + " debe tener atributo BRAND");

                for (AttributeDto attribute : product.getAttributes()) {
                    assertNotNull(attribute.getId(), "Attribute ID no debe ser null");
                    assertNotNull(attribute.getName(), "Attribute name no debe ser null");
                    assertNotNull(attribute.getValueName(), "Attribute value name no debe ser null");

                    assertFalse(attribute.getId().isBlank(), "Attribute ID no debe estar vacío");
                    assertFalse(attribute.getName().isBlank(), "Attribute name no debe estar vacío");
                    assertFalse(attribute.getValueName().isBlank(), "Attribute value name no debe estar vacío");
                }
            }
        }
    }

    @Test
    @DisplayName("Debe validar las variaciones de los productos")
    void shouldValidateProductVariations() {
        List<ProductDto> products = productsContainer.getProducts();

        for (ProductDto product : products) {
            if (product.getVariations() != null && !product.getVariations().isEmpty()) {
                for (VariationDto variation : product.getVariations()) {
                    assertNotNull(variation.getId(), "Variation ID no debe ser null");
                    assertNotNull(variation.getPrice(), "Variation price no debe ser null");
                    assertNotNull(variation.getAvailableQuantity(), "Variation available quantity no debe ser null");

                    // Validar precio positivo
                    assertTrue(variation.getPrice().compareTo(BigDecimal.ZERO) > 0,
                            "El precio de la variación debe ser mayor a 0");

                    // Validar cantidad no negativa
                    assertTrue(variation.getAvailableQuantity() >= 0,
                            "La cantidad disponible no puede ser negativa");

                    // Validar combinaciones de atributos
                    if (variation.getAttributeCombinations() != null) {
                        for (AttributeCombinationDto combination : variation.getAttributeCombinations()) {
                            assertNotNull(combination.getName(), "Combination name no debe ser null");
                            assertNotNull(combination.getValueName(), "Combination value name no debe ser null");
                            assertFalse(combination.getName().isBlank(), "Combination name no debe estar vacío");
                            assertFalse(combination.getValueName().isBlank(), "Combination value name no debe estar vacío");
                        }
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Debe validar marcas específicas en los productos")
    void shouldValidateSpecificBrands() {
        List<ProductDto> products = productsContainer.getProducts();

        // Verificar que tenemos productos de diferentes marcas
        boolean hasNike = products.stream()
                .flatMap(p -> p.getAttributes().stream())
                .anyMatch(attr -> "BRAND".equals(attr.getId()) && "Nike".equals(attr.getValueName()));

        boolean hasApple = products.stream()
                .flatMap(p -> p.getAttributes().stream())
                .anyMatch(attr -> "BRAND".equals(attr.getId()) && "Apple".equals(attr.getValueName()));

        boolean hasLenovo = products.stream()
                .flatMap(p -> p.getAttributes().stream())
                .anyMatch(attr -> "BRAND".equals(attr.getId()) && "Lenovo".equals(attr.getValueName()));

        boolean hasSony = products.stream()
                .flatMap(p -> p.getAttributes().stream())
                .anyMatch(attr -> "BRAND".equals(attr.getId()) && "Sony".equals(attr.getValueName()));

        boolean hasAdidas = products.stream()
                .flatMap(p -> p.getAttributes().stream())
                .anyMatch(attr -> "BRAND".equals(attr.getId()) && "Adidas".equals(attr.getValueName()));

        assertTrue(hasNike, "Debe haber un producto Nike");
        assertTrue(hasApple, "Debe haber un producto Apple");
        assertTrue(hasLenovo, "Debe haber un producto Lenovo");
        assertTrue(hasSony, "Debe haber un producto Sony");
        assertTrue(hasAdidas, "Debe haber un producto Adidas");

    }

    @Test
    @DisplayName("Debe validar diferentes tipos de variaciones")
    void shouldValidateDifferentVariationTypes() {
        List<ProductDto> products = productsContainer.getProducts();

        // Buscar producto con variaciones de talle
        boolean hasSizeVariations = products.stream()
                .flatMap(p -> p.getVariations().stream())
                .flatMap(v -> v.getAttributeCombinations().stream())
                .anyMatch(ac -> "Talle".equals(ac.getName()));

        // Buscar producto con variaciones de color
        boolean hasColorVariations = products.stream()
                .flatMap(p -> p.getVariations().stream())
                .flatMap(v -> v.getAttributeCombinations().stream())
                .anyMatch(ac -> "Color".equals(ac.getName()));

        // Buscar producto con variaciones de almacenamiento
        boolean hasStorageVariations = products.stream()
                .flatMap(p -> p.getVariations().stream())
                .flatMap(v -> v.getAttributeCombinations().stream())
                .anyMatch(ac -> "Almacenamiento".equals(ac.getName()));

        assertTrue(hasSizeVariations, "Debe haber productos con variaciones de talle");
        assertTrue(hasColorVariations, "Debe haber productos con variaciones de color");
        assertTrue(hasStorageVariations, "Debe haber productos con variaciones de almacenamiento");

    }

    @Test
    @DisplayName("Debe imprimir resumen completo de productos cargados")
    void shouldPrintProductSummary() {
        List<ProductDto> products = productsContainer.getProducts();

        // Aserciones para validar que hay productos válidos
        assertNotNull(products, "La lista de productos no debe ser null");
        assertFalse(products.isEmpty(), "Debe haber al menos un producto para mostrar resumen");
        assertTrue(products.size() >= 5, "Debe haber al menos 5 productos en el JSON de ejemplo");

        System.out.println("\n RESUMEN DE PRODUCTOS CARGADOS:");

        for (int i = 0; i < products.size(); i++) {
            ProductDto product = products.get(i);
            System.out.printf("%d. %s (%s)%n", i + 1, product.getTitle(), product.getId());
            System.out.printf("    Precio: %s %s%n", product.getPrice(), product.getCurrencyId());
            System.out.printf("    Estado: %s | Condición: %s%n", product.getStatus(), product.getCondition());

            if (product.getAttributes() != null) {
                System.out.printf("     Atributos: %d%n", product.getAttributes().size());
            }

            if (product.getVariations() != null) {
                System.out.printf("    Variaciones: %d%n", product.getVariations().size());
            }

            if (product.getPictures() != null) {
                System.out.printf("    Imágenes: %d%n", product.getPictures().size());
            }

            System.out.println();
        }

        System.out.printf("Total: %d productos cargados exitosamente%n", products.size());

        // Aserción final para confirmar que se ejecutó el resumen
        assertEquals(5, products.size(), "Debería haber exactamente 5 productos en el JSON de ejemplo");
    }
}