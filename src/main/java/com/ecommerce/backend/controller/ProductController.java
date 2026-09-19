package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDTO;
import com.ecommerce.backend.dto.ProductResponseDTO;
import com.ecommerce.backend.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Add Product
    @PostMapping
    public ProductResponseDTO addProduct(
            @Valid @RequestBody ProductDTO productDTO) {

        return productService.addProduct(productDTO);
    }

    // Get All Products
    @GetMapping
    public List<ProductResponseDTO> getAllProducts() {

        return productService.getAllProducts();
    }

    // Get All Products - Pagination
    @GetMapping("/page")
    public Page<ProductResponseDTO> getProductsWithPagination(
            Pageable pageable) {

        return productService.getAllProducts(pageable);
    }

    // Get Product By ID
    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(
            @PathVariable Long id) {

        return productService.getProductById(id);
    }

    // Update Product
    @PutMapping("/{id}")
    public ProductResponseDTO updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        return productService.updateProduct(id, productDTO);
    }

    // Delete Product
    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable Long id) {

        productService.deleteProduct(id);

        return "Product deleted successfully";
    }

    // Search Products By Name
    @GetMapping("/search")
    public List<ProductResponseDTO> searchProducts(
            @RequestParam String name) {

        return productService.searchProducts(name);
    }

    // Filter Products By Price
    @GetMapping("/filter")
    public List<ProductResponseDTO> filterProductsByPrice(
            @RequestParam double minPrice,
            @RequestParam double maxPrice) {

        return productService.filterProductsByPrice(
                minPrice,
                maxPrice);
    }

    // Sort Products Low to High
    @GetMapping("/sort/low-to-high")
    public List<ProductResponseDTO> sortProductsLowToHigh() {

        return productService.sortProductsLowToHigh();
    }

    // Sort Products High to Low
    @GetMapping("/sort/high-to-low")
    public List<ProductResponseDTO> sortProductsHighToLow() {

        return productService.sortProductsHighToLow();
    }
}