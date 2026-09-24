package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDTO;
import com.ecommerce.backend.dto.CartResponseDTO;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ProductNotFoundException;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private static final Logger logger =
            LoggerFactory.getLogger(CartService.class);

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

        logger.info(
                "Adding product {} to cart for user: {}",
                cartDTO.getProductId(),
                email);

        User user = userService.findByEmail(email);

        Product product = productRepository
                .findById(cartDTO.getProductId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Product not found while adding to cart: {}",
                            cartDTO.getProductId());

                    return new ProductNotFoundException(
                            "Product not found");
                });

        if (product.getQuantity() < cartDTO.getQuantity()) {

            logger.warn(
                    "Insufficient stock for product: {}",
                    cartDTO.getProductId());

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

                logger.warn(
                        "Insufficient stock while increasing cart quantity. Product: {}",
                        cartDTO.getProductId());

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

        logger.info(
                "Product added to cart successfully. Cart id: {}",
                savedCart.getId());

        return convertToResponse(savedCart);
    }

    // Get user's cart
    public List<CartResponseDTO> getCart(
            String email) {

        logger.info(
                "Fetching cart for user: {}",
                email);

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

        logger.info(
                "Updating cart {} quantity to {} for user: {}",
                cartId,
                quantity,
                email);

        if (quantity <= 0) {

            logger.warn(
                    "Invalid cart quantity: {}",
                    quantity);

            throw new IllegalArgumentException(
                    "Quantity must be greater than 0");
        }

        User user = userService.findByEmail(email);

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Cart item not found: {}",
                            cartId);

                    return new IllegalArgumentException(
                            "Cart item not found");
                });

        if (!user.getId().equals(cart.getUserId())) {

            logger.warn(
                    "Unauthorized cart update attempt. User: {}, cartId: {}",
                    email,
                    cartId);

            throw new IllegalArgumentException(
                    "You can only update your own cart");
        }

        Product product = productRepository
                .findById(cart.getProductId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Product not found while updating cart: {}",
                            cart.getProductId());

                    return new ProductNotFoundException(
                            "Product not found");
                });

        if (product.getQuantity() < quantity) {

            logger.warn(
                    "Insufficient stock while updating cart: {}",
                    cart.getProductId());

            throw new IllegalArgumentException(
                    "Insufficient stock");
        }

        cart.setQuantity(quantity);

        Cart updatedCart =
                cartRepository.save(cart);

        logger.info(
                "Cart updated successfully. Cart id: {}",
                cartId);

        return convertToResponse(updatedCart);
    }

    // Remove item from cart
    public void removeFromCart(
            String email,
            Long cartId) {

        logger.info(
                "Removing cart item {} for user: {}",
                cartId,
                email);

        User user = userService.findByEmail(email);

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Cart item not found: {}",
                            cartId);

                    return new IllegalArgumentException(
                            "Cart item not found");
                });

        if (!user.getId().equals(cart.getUserId())) {

            logger.warn(
                    "Unauthorized cart removal attempt. User: {}, cartId: {}",
                    email,
                    cartId);

            throw new IllegalArgumentException(
                    "You can only remove your own cart item");
        }

        cartRepository.delete(cart);

        logger.info(
                "Cart item removed successfully: {}",
                cartId);
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