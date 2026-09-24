package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDTO;
import com.ecommerce.backend.dto.ProductResponseDTO;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ProductNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Add Product
    public ProductResponseDTO addProduct(ProductDTO productDTO) {

        logger.info("Adding new product: {}", productDTO.getName());

        Product product = new Product();

        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());

        Product savedProduct = productRepository.save(product);

        logger.info("Product created successfully with id: {}",
                savedProduct.getId());

        return convertToResponseDTO(savedProduct);
    }

    // Get All Products
    public List<ProductResponseDTO> getAllProducts() {

        logger.info("Fetching all products");

        return productRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Get All Products - Pagination
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {

        logger.info(
                "Fetching products with pagination - page: {}, size: {}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

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

        logger.info("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("Product not found with id: {}", id);

                    return new ProductNotFoundException(
                            "Product not found with id: " + id
                    );
                });

        return convertToResponseDTO(product);
    }

    // Update Product
    public ProductResponseDTO updateProduct(
            Long id,
            ProductDTO productDTO) {

        logger.info("Updating product with id: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {

                    logger.warn("Cannot update. Product not found with id: {}", id);

                    return new ProductNotFoundException(
                            "Product not found with id: " + id
                    );
                });

        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setDescription(productDTO.getDescription());

        Product updatedProduct =
                productRepository.save(existingProduct);

        logger.info("Product updated successfully with id: {}",
                updatedProduct.getId());

        return convertToResponseDTO(updatedProduct);
    }

    // Delete Product
    public void deleteProduct(Long id) {

        logger.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {

            logger.warn("Cannot delete. Product not found with id: {}", id);

            throw new ProductNotFoundException(
                    "Product not found with id: " + id
            );
        }

        productRepository.deleteById(id);

        logger.info("Product deleted successfully with id: {}", id);
    }

    // Search Products By Name
    public List<ProductResponseDTO> searchProducts(String name) {

        logger.info("Searching products by name: {}", name);

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

        logger.info(
                "Filtering products by price range: {} - {}",
                minPrice,
                maxPrice
        );

        return productRepository
                .findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Sort Products By Price - Low to High
    public List<ProductResponseDTO> sortProductsLowToHigh() {

        logger.info("Sorting products by price: low to high");

        return productRepository
                .findAllByOrderByPriceAsc()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    // Sort Products By Price - High to Low
    public List<ProductResponseDTO> sortProductsHighToLow() {

        logger.info("Sorting products by price: high to low");

        return productRepository
                .findAllByOrderByPriceDesc()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }
}