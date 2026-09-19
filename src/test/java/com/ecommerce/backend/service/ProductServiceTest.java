package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDTO;
import com.ecommerce.backend.dto.ProductResponseDTO;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ProductNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // Test 1: Add Product
    @Test
    void addProduct_shouldSaveAndReturnProduct() {

        ProductDTO productDTO = new ProductDTO();

        productDTO.setName("Test Laptop");
        productDTO.setPrice(50000);
        productDTO.setQuantity(5);
        productDTO.setDescription("Test Laptop Description");

        Product savedProduct = new Product();

        savedProduct.setId(1L);
        savedProduct.setName("Test Laptop");
        savedProduct.setPrice(50000);
        savedProduct.setQuantity(5);
        savedProduct.setDescription("Test Laptop Description");

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        ProductResponseDTO response =
                productService.addProduct(productDTO);

        assertEquals("Test Laptop", response.getName());
        assertEquals(50000, response.getPrice());
        assertEquals(5, response.getQuantity());
        assertEquals(
                "Test Laptop Description",
                response.getDescription()
        );
    }

    // Test 2: Get Product By ID - Product Exists
    @Test
    void getProductById_shouldReturnProduct() {

        Product product = new Product();

        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(5);
        product.setDescription("Test Laptop Description");

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponseDTO response =
                productService.getProductById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Test Laptop", response.getName());
        assertEquals(50000, response.getPrice());
        assertEquals(5, response.getQuantity());
        assertEquals(
                "Test Laptop Description",
                response.getDescription()
        );
    }

    // Test 3: Get Product By ID - Product Not Found
    @Test
    void getProductById_shouldThrowExceptionWhenProductNotFound() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(99L)
        );
    }
    // Test 4: Update Product
@Test
void updateProduct_shouldUpdateAndReturnProduct() {

    ProductDTO productDTO = new ProductDTO();

    productDTO.setName("Updated Laptop");
    productDTO.setPrice(60000);
    productDTO.setQuantity(10);
    productDTO.setDescription("Updated Laptop Description");

    Product existingProduct = new Product();

    existingProduct.setId(1L);
    existingProduct.setName("Old Laptop");
    existingProduct.setPrice(50000);
    existingProduct.setQuantity(5);
    existingProduct.setDescription("Old Description");

    Product updatedProduct = new Product();

    updatedProduct.setId(1L);
    updatedProduct.setName("Updated Laptop");
    updatedProduct.setPrice(60000);
    updatedProduct.setQuantity(10);
    updatedProduct.setDescription("Updated Laptop Description");

    when(productRepository.findById(1L))
            .thenReturn(Optional.of(existingProduct));

    when(productRepository.save(any(Product.class)))
            .thenReturn(updatedProduct);

    ProductResponseDTO response =
            productService.updateProduct(1L, productDTO);

    assertEquals(1L, response.getId());
    assertEquals("Updated Laptop", response.getName());
    assertEquals(60000, response.getPrice());
    assertEquals(10, response.getQuantity());
    assertEquals(
            "Updated Laptop Description",
            response.getDescription()
    );
}
// Test 5: Delete Product - Product Exists
@Test
void deleteProduct_shouldDeleteProduct() {

    when(productRepository.existsById(1L))
            .thenReturn(true);

    productService.deleteProduct(1L);

    org.mockito.Mockito.verify(productRepository)
            .deleteById(1L);
}


// Test 6: Delete Product - Product Not Found
@Test
void deleteProduct_shouldThrowExceptionWhenProductNotFound() {

    when(productRepository.existsById(99L))
            .thenReturn(false);

    assertThrows(
            ProductNotFoundException.class,
            () -> productService.deleteProduct(99L)
    );
}
// Test 7: Get All Products
@Test
void getAllProducts_shouldReturnAllProducts() {

    Product product1 = new Product();

    product1.setId(1L);
    product1.setName("Laptop");
    product1.setPrice(50000);
    product1.setQuantity(5);
    product1.setDescription("HP Laptop");


    Product product2 = new Product();

    product2.setId(2L);
    product2.setName("Mobile");
    product2.setPrice(25000);
    product2.setQuantity(10);
    product2.setDescription("Samsung Mobile");


    when(productRepository.findAll())
            .thenReturn(java.util.List.of(product1, product2));


    java.util.List<ProductResponseDTO> response =
            productService.getAllProducts();


    assertEquals(2, response.size());

    assertEquals(1L, response.get(0).getId());
    assertEquals("Laptop", response.get(0).getName());
    assertEquals(50000, response.get(0).getPrice());

    assertEquals(2L, response.get(1).getId());
    assertEquals("Mobile", response.get(1).getName());
    assertEquals(25000, response.get(1).getPrice());
}
// Test 8: Search Products By Name
@Test
void searchProducts_shouldReturnMatchingProducts() {

    Product product1 = new Product();

    product1.setId(1L);
    product1.setName("HP Laptop");
    product1.setPrice(50000);
    product1.setQuantity(5);
    product1.setDescription("HP Laptop");


    Product product2 = new Product();

    product2.setId(2L);
    product2.setName("HP Gaming Laptop");
    product2.setPrice(70000);
    product2.setQuantity(3);
    product2.setDescription("Gaming Laptop");


    when(productRepository.findByNameContainingIgnoreCase("Laptop"))
            .thenReturn(java.util.List.of(product1, product2));


    java.util.List<ProductResponseDTO> response =
            productService.searchProducts("Laptop");


    assertEquals(2, response.size());

    assertEquals("HP Laptop", response.get(0).getName());
    assertEquals(50000, response.get(0).getPrice());

    assertEquals("HP Gaming Laptop", response.get(1).getName());
    assertEquals(70000, response.get(1).getPrice());
}
// Test 9: Filter Products By Price
@Test
void filterProductsByPrice_shouldReturnMatchingProducts() {

    Product product1 = new Product();

    product1.setId(1L);
    product1.setName("Laptop");
    product1.setPrice(50000);
    product1.setQuantity(5);
    product1.setDescription("HP Laptop");


    Product product2 = new Product();

    product2.setId(2L);
    product2.setName("Mobile");
    product2.setPrice(30000);
    product2.setQuantity(10);
    product2.setDescription("Samsung Mobile");


    when(productRepository.findByPriceBetween(20000, 60000))
            .thenReturn(java.util.List.of(product1, product2));


    java.util.List<ProductResponseDTO> response =
            productService.filterProductsByPrice(20000, 60000);


    assertEquals(2, response.size());

    assertEquals("Laptop", response.get(0).getName());
    assertEquals(50000, response.get(0).getPrice());

    assertEquals("Mobile", response.get(1).getName());
    assertEquals(30000, response.get(1).getPrice());
}
// Test 10: Sort Products By Price - Low to High
@Test
void sortProductsLowToHigh_shouldReturnProductsInAscendingOrder() {

    Product product1 = new Product();

    product1.setId(1L);
    product1.setName("Laptop");
    product1.setPrice(50000);
    product1.setQuantity(5);
    product1.setDescription("HP Laptop");


    Product product2 = new Product();

    product2.setId(2L);
    product2.setName("Mobile");
    product2.setPrice(30000);
    product2.setQuantity(10);
    product2.setDescription("Samsung Mobile");


    when(productRepository.findAllByOrderByPriceAsc())
            .thenReturn(java.util.List.of(product2, product1));


    java.util.List<ProductResponseDTO> response =
            productService.sortProductsLowToHigh();


    assertEquals(2, response.size());

    assertEquals("Mobile", response.get(0).getName());
    assertEquals(30000, response.get(0).getPrice());

    assertEquals("Laptop", response.get(1).getName());
    assertEquals(50000, response.get(1).getPrice());
}
// Test 11: Sort Products By Price - High to Low
@Test
void sortProductsHighToLow_shouldReturnProductsInDescendingOrder() {

    Product product1 = new Product();

    product1.setId(1L);
    product1.setName("Laptop");
    product1.setPrice(50000);
    product1.setQuantity(5);
    product1.setDescription("HP Laptop");


    Product product2 = new Product();

    product2.setId(2L);
    product2.setName("Mobile");
    product2.setPrice(30000);
    product2.setQuantity(10);
    product2.setDescription("Samsung Mobile");


    when(productRepository.findAllByOrderByPriceDesc())
            .thenReturn(java.util.List.of(product1, product2));


    java.util.List<ProductResponseDTO> response =
            productService.sortProductsHighToLow();


    assertEquals(2, response.size());

    assertEquals("Laptop", response.get(0).getName());
    assertEquals(50000, response.get(0).getPrice());

    assertEquals("Mobile", response.get(1).getName());
    assertEquals(30000, response.get(1).getPrice());
}
}
