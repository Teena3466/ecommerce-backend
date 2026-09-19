package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDTO;
import com.ecommerce.backend.dto.CartResponseDTO;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ProductNotFoundException;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public CartService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserService userService) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    // Add product to cart
    public CartResponseDTO addToCart(
            String email,
            CartDTO cartDTO) {

        User user = userService.findByEmail(email);

        Product product = productRepository
                .findById(cartDTO.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found"));

        if (product.getQuantity() < cartDTO.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock");
        }

        Cart cart = cartRepository
                .findByUserIdAndProductId(
                        user.getId(),
                        cartDTO.getProductId())
                .orElse(null);

        if (cart != null) {

            int newQuantity =
                    cart.getQuantity()
                            + cartDTO.getQuantity();

            if (product.getQuantity() < newQuantity) {
                throw new IllegalArgumentException(
                        "Insufficient stock");
            }

            cart.setQuantity(newQuantity);

        } else {

            cart = new Cart();

            cart.setUserId(user.getId());
            cart.setProductId(cartDTO.getProductId());
            cart.setQuantity(cartDTO.getQuantity());
        }

        Cart savedCart =
                cartRepository.save(cart);

        return convertToResponse(savedCart);
    }

    // Get user's cart
    public List<CartResponseDTO> getCart(
            String email) {

        User user = userService.findByEmail(email);

        return cartRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Update cart quantity
    public CartResponseDTO updateCartQuantity(
            String email,
            Long cartId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        User user = userService.findByEmail(email);

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found"));

        if (!user.getId().equals(cart.getUserId())) {
            throw new IllegalArgumentException(
                    "You can only update your own cart");
        }

        Product product = productRepository
                .findById(cart.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found"));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock");
        }

        cart.setQuantity(quantity);

        Cart updatedCart =
                cartRepository.save(cart);

        return convertToResponse(updatedCart);
    }

    // Remove item from cart
    public void removeFromCart(
            String email,
            Long cartId) {

        User user = userService.findByEmail(email);

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cart item not found"));

        if (!user.getId().equals(cart.getUserId())) {
            throw new IllegalArgumentException(
                    "You can only remove your own cart item");
        }

        cartRepository.delete(cart);
    }

    // Convert Cart to Response DTO
    private CartResponseDTO convertToResponse(
            Cart cart) {

        Product product = productRepository
                .findById(cart.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found"));

        double totalPrice =
                product.getPrice()
                        * cart.getQuantity();

        return new CartResponseDTO(
                cart.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cart.getQuantity(),
                totalPrice
        );
    }
}