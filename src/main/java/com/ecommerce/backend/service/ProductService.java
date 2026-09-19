package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDTO;
import com.ecommerce.backend.dto.ProductResponseDTO;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ProductNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Add Product
    public ProductResponseDTO addProduct(ProductDTO productDTO) {

        Product product = new Product();

        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());

        Product savedProduct = productRepository.save(product);

        return convertToResponseDTO(savedProduct);
    }

    // Get All Products
    public List<ProductResponseDTO> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Get All Products - Pagination
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {

        return productRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    // Convert Entity to Response DTO
    private ProductResponseDTO convertToResponseDTO(Product product) {

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription()
        );
    }

    // Get Product By ID
    public ProductResponseDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        return convertToResponseDTO(product);
    }

    // Update Product
    public ProductResponseDTO updateProduct(
            Long id,
            ProductDTO productDTO) {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setDescription(productDTO.getDescription());

        Product updatedProduct =
                productRepository.save(existingProduct);

        return convertToResponseDTO(updatedProduct);
    }

    // Delete Product
    public void deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {

            throw new ProductNotFoundException(
                    "Product not found with id: " + id);
        }

        productRepository.deleteById(id);
    }

    // Search Products By Name
    public List<ProductResponseDTO> searchProducts(String name) {

        return productRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Filter Products By Price
    public List<ProductResponseDTO> filterProductsByPrice(
            double minPrice,
            double maxPrice) {

        return productRepository
                .findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Sort Products By Price - Low to High
    public List<ProductResponseDTO> sortProductsLowToHigh() {

        return productRepository
                .findAllByOrderByPriceAsc()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Sort Products By Price - High to Low
    public List<ProductResponseDTO> sortProductsHighToLow() {

        return productRepository
                .findAllByOrderByPriceDesc()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }
}