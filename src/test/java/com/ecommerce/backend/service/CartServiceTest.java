package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDTO;
import com.ecommerce.backend.dto.CartResponseDTO;
import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.exception.ProductNotFoundException;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartService cartService;


    // =========================================================
    // Test 1: Add Product To Cart - Success
    // =========================================================
    @Test
    void addToCart_shouldAddProductSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole("USER");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(10);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductId(1L);
        cartDTO.setQuantity(2);

        Cart savedCart = new Cart();
        savedCart.setId(1L);
        savedCart.setUserId(1L);
        savedCart.setProductId(1L);
        savedCart.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserIdAndProductId(1L, 1L))
                .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(savedCart);

        CartResponseDTO response =
                cartService.addToCart(
                        "test@example.com",
                        cartDTO
                );

        assertEquals(1L, response.getId());
        assertEquals(1L, response.getProductId());
        assertEquals("Test Laptop", response.getProductName());
        assertEquals(50000, response.getPrice());
        assertEquals(2, response.getQuantity());
        assertEquals(100000, response.getTotalPrice());
    }


    // =========================================================
    // Test 2: Add Product - Product Not Found
    // =========================================================
    @Test
    void addToCart_shouldThrowExceptionWhenProductNotFound() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductId(99L);
        cartDTO.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> cartService.addToCart(
                        "test@example.com",
                        cartDTO
                )
        );
    }


    // =========================================================
    // Test 3: Add Product - Insufficient Stock
    // =========================================================
    @Test
    void addToCart_shouldThrowExceptionWhenStockIsInsufficient() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(2);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductId(1L);
        cartDTO.setQuantity(5);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart(
                        "test@example.com",
                        cartDTO
                )
        );
    }


    // =========================================================
    // Test 4: Add Product Already In Cart - Success
    // =========================================================
    @Test
    void addToCart_shouldIncreaseQuantityWhenProductAlreadyExists() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(10);

        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setUserId(1L);
        existingCart.setProductId(1L);
        existingCart.setQuantity(2);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductId(1L);
        cartDTO.setQuantity(3);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(existingCart));

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(existingCart);

        CartResponseDTO response =
                cartService.addToCart(
                        "test@example.com",
                        cartDTO
                );

        assertEquals(5, response.getQuantity());
        assertEquals(250000, response.getTotalPrice());
    }


    // =========================================================
    // Test 5: Add Product Already In Cart - Insufficient Stock
    // =========================================================
    @Test
    void addToCart_shouldThrowExceptionWhenNewQuantityExceedsStock() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(5);

        Cart existingCart = new Cart();
        existingCart.setId(1L);
        existingCart.setUserId(1L);
        existingCart.setProductId(1L);
        existingCart.setQuantity(4);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setProductId(1L);
        cartDTO.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUserIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(existingCart));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCart(
                        "test@example.com",
                        cartDTO
                )
        );
    }


    // =========================================================
    // Test 6: Get Cart
    // =========================================================
    @Test
    void getCart_shouldReturnUserCart() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(10);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findByUserId(1L))
                .thenReturn(List.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        List<CartResponseDTO> response =
                cartService.getCart("test@example.com");

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(1L, response.get(0).getProductId());
        assertEquals("Test Laptop", response.get(0).getProductName());
        assertEquals(2, response.get(0).getQuantity());
        assertEquals(100000, response.get(0).getTotalPrice());
    }


    // =========================================================
    // Test 7: Get Cart - Empty
    // =========================================================
    @Test
    void getCart_shouldReturnEmptyListWhenCartIsEmpty() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findByUserId(1L))
                .thenReturn(List.of());

        List<CartResponseDTO> response =
                cartService.getCart("test@example.com");

        assertEquals(0, response.size());
    }


    // =========================================================
    // Test 8: Update Cart Quantity - Success
    // =========================================================
    @Test
    void updateCartQuantity_shouldUpdateSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(10);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(cart);

        CartResponseDTO response =
                cartService.updateCartQuantity(
                        "test@example.com",
                        1L,
                        5
                );

        assertEquals(5, response.getQuantity());
        assertEquals(250000, response.getTotalPrice());
    }


    // =========================================================
    // Test 9: Update Cart - Invalid Quantity
    // =========================================================
    @Test
    void updateCartQuantity_shouldThrowExceptionWhenQuantityIsInvalid() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartQuantity(
                        "test@example.com",
                        1L,
                        0
                )
        );
    }


    // =========================================================
    // Test 10: Update Cart - Cart Not Found
    // =========================================================
    @Test
    void updateCartQuantity_shouldThrowExceptionWhenCartNotFound() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartQuantity(
                        "test@example.com",
                        99L,
                        2
                )
        );
    }


    // =========================================================
    // Test 11: Update Cart - Unauthorized User
    // =========================================================
    @Test
    void updateCartQuantity_shouldThrowExceptionForAnotherUsersCart() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartQuantity(
                        "test@example.com",
                        1L,
                        3
                )
        );
    }


    // =========================================================
    // Test 12: Update Cart - Product Not Found
    // =========================================================
    @Test
    void updateCartQuantity_shouldThrowExceptionWhenProductNotFound() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setProductId(99L);
        cart.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> cartService.updateCartQuantity(
                        "test@example.com",
                        1L,
                        3
                )
        );
    }


    // =========================================================
    // Test 13: Update Cart - Insufficient Stock
    // =========================================================
    @Test
    void updateCartQuantity_shouldThrowExceptionWhenStockIsInsufficient() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Laptop");
        product.setPrice(50000);
        product.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateCartQuantity(
                        "test@example.com",
                        1L,
                        5
                )
        );
    }


    // =========================================================
    // Test 14: Remove Cart Item - Success
    // =========================================================
    @Test
    void removeFromCart_shouldRemoveCartItemSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        cartService.removeFromCart(
                "test@example.com",
                1L
        );

        verify(cartRepository).delete(cart);
    }


    // =========================================================
    // Test 15: Remove Cart Item - Cart Not Found
    // =========================================================
    @Test
    void removeFromCart_shouldThrowExceptionWhenCartNotFound() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.removeFromCart(
                        "test@example.com",
                        99L
                )
        );
    }


    // =========================================================
    // Test 16: Remove Cart Item - Unauthorized User
    // =========================================================
    @Test
    void removeFromCart_shouldThrowExceptionForAnotherUsersCart() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);
        cart.setProductId(1L);
        cart.setQuantity(2);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.removeFromCart(
                        "test@example.com",
                        1L
                )
        );
    }
}