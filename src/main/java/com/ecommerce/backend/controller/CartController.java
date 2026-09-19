package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartDTO;
import com.ecommerce.backend.dto.CartResponseDTO;
import com.ecommerce.backend.service.CartService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Add product to cart
    @PostMapping
    public CartResponseDTO addToCart(
            @Valid @RequestBody CartDTO cartDTO,
            Authentication authentication) {

        String email = authentication.getName();

        return cartService.addToCart(email, cartDTO);
    }

    // Get logged-in user's cart
    @GetMapping
    public List<CartResponseDTO> getCart(
            Authentication authentication) {

        String email = authentication.getName();

        return cartService.getCart(email);
    }
    @PutMapping("/{cartId}")
public CartResponseDTO updateCartQuantity(
        @PathVariable Long cartId,
        @RequestParam int quantity,
        Authentication authentication) {

    String email = authentication.getName();

    return cartService.updateCartQuantity(
            email,
            cartId,
            quantity
    );
}
@DeleteMapping("/{cartId}")
public String removeFromCart(
        @PathVariable Long cartId,
        Authentication authentication) {

    String email = authentication.getName();

    cartService.removeFromCart(
            email,
            cartId
    );

    return "Cart item removed successfully";
}
}