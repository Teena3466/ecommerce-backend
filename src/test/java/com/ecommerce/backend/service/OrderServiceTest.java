package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.OrderRepository;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;


    // Test 1: Create Order
    @Test
    void createOrder_shouldCreateAndReturnOrder() {

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
        product.setDescription("Test Laptop");


        Order savedOrder = new Order();

        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setProduct(product);
        savedOrder.setQuantity(2);
        savedOrder.setTotalPrice(100000);
        savedOrder.setStatus(OrderStatus.PLACED);


        when(userService.findByEmail("test@example.com"))
                .thenReturn(user);

        when(productRepository.findById(1L))
                .thenReturn(java.util.Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(savedOrder);


        Order response =
                orderService.createOrder(
                        "test@example.com",
                        1L,
                        2
                );


        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUser().getId());
        assertEquals(1L, response.getProduct().getId());
        assertEquals(2, response.getQuantity());
        assertEquals(100000, response.getTotalPrice());
        assertEquals(OrderStatus.PLACED, response.getStatus());
    }
    // Test 2: Create Order - Product Not Found
@Test
void createOrder_shouldThrowExceptionWhenProductNotFound() {

    User user = new User();

    user.setId(1L);
    user.setName("Test User");
    user.setEmail("test@example.com");
    user.setRole("USER");


    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(productRepository.findById(99L))
            .thenReturn(java.util.Optional.empty());


    assertThrows(
            com.ecommerce.backend.exception.ProductNotFoundException.class,
            () -> orderService.createOrder(
                    "test@example.com",
                    99L,
                    2
            )
    );
}
// Test 3: Create Order - Insufficient Stock
@Test
void createOrder_shouldThrowExceptionWhenStockIsInsufficient() {

    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setEmail("test@example.com");
    user.setRole("USER");

    Product product = new Product();
    product.setId(1L);
    product.setName("Test Laptop");
    product.setPrice(50000);
    product.setQuantity(2);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(productRepository.findById(1L))
            .thenReturn(java.util.Optional.of(product));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(
                    "test@example.com",
                    1L,
                    5
            )
    );
}
// Test 4: Create Order - Invalid Quantity
@Test
void createOrder_shouldThrowExceptionWhenQuantityIsInvalid() {

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(
                    "test@example.com",
                    1L,
                    0
            )
    );
}
// Test 5: Checkout Cart - Empty Cart
@Test
void checkoutCart_shouldThrowExceptionWhenCartIsEmpty() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(cartRepository.findByUserId(1L))
            .thenReturn(java.util.Collections.emptyList());

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.checkoutCart("test@example.com")
    );
}
// Test 6: Checkout Cart - Insufficient Stock
@Test
void checkoutCart_shouldThrowExceptionWhenStockIsInsufficient() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    Cart cart = new Cart();
    cart.setUserId(1L);
    cart.setProductId(1L);
    cart.setQuantity(5);

    Product product = new Product();
    product.setId(1L);
    product.setName("Test Laptop");
    product.setPrice(50000);
    product.setQuantity(2);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(cartRepository.findByUserId(1L))
            .thenReturn(java.util.List.of(cart));

    when(productRepository.findById(1L))
            .thenReturn(java.util.Optional.of(product));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.checkoutCart("test@example.com")
    );
}
// Test 7: Checkout Cart - Success
@Test
void checkoutCart_shouldCreateOrdersAndClearCart() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    Cart cart = new Cart();
    cart.setUserId(1L);
    cart.setProductId(1L);
    cart.setQuantity(2);

    Product product = new Product();
    product.setId(1L);
    product.setName("Test Laptop");
    product.setPrice(50000);
    product.setQuantity(10);

    Order savedOrder = new Order();
    savedOrder.setId(1L);
    savedOrder.setUser(user);
    savedOrder.setProduct(product);
    savedOrder.setQuantity(2);
    savedOrder.setTotalPrice(100000);
    savedOrder.setStatus(OrderStatus.PLACED);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(cartRepository.findByUserId(1L))
            .thenReturn(java.util.List.of(cart));

    when(productRepository.findById(1L))
            .thenReturn(java.util.Optional.of(product));

    when(orderRepository.save(any(Order.class)))
            .thenReturn(savedOrder);

    java.util.List<Order> response =
            orderService.checkoutCart("test@example.com");

    assertEquals(1, response.size());
    assertEquals(1L, response.get(0).getId());
    assertEquals(2, response.get(0).getQuantity());
    assertEquals(100000, response.get(0).getTotalPrice());
    assertEquals(OrderStatus.PLACED, response.get(0).getStatus());

    assertEquals(8, product.getQuantity());

    org.mockito.Mockito.verify(cartRepository)
            .deleteAll(java.util.List.of(cart));
}
// Test 8: Get Orders By User - Success
@Test
void getOrdersByUser_shouldReturnOrdersForUser() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    Order order = new Order();
    order.setId(1L);
    order.setUser(user);
    order.setQuantity(2);
    order.setTotalPrice(100000);
    order.setStatus(OrderStatus.PLACED);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(orderRepository.findByUserId(1L))
            .thenReturn(java.util.List.of(order));

    java.util.List<Order> response =
            orderService.getOrdersByUser(1L, "test@example.com");

    assertEquals(1, response.size());
    assertEquals(1L, response.get(0).getId());
    assertEquals(1L, response.get(0).getUser().getId());
    assertEquals(OrderStatus.PLACED, response.get(0).getStatus());
}
// Test 9: Get Orders By User - Unauthorized User
@Test
void getOrdersByUser_shouldThrowExceptionWhenUserAccessesAnotherUsersOrders() {

    User loggedInUser = new User();
    loggedInUser.setId(1L);
    loggedInUser.setEmail("test@example.com");
    loggedInUser.setRole("USER");

    when(userService.findByEmail("test@example.com"))
            .thenReturn(loggedInUser);

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.getOrdersByUser(
                    2L,
                    "test@example.com"
            )
    );
}
// Test 10: Cancel Order - Order Not Found
@Test
void cancelOrder_shouldThrowExceptionWhenOrderNotFound() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(orderRepository.findById(99L))
            .thenReturn(java.util.Optional.empty());

    assertThrows(
            com.ecommerce.backend.exception.OrderNotFoundException.class,
            () -> orderService.cancelOrder(
                    99L,
                    "test@example.com"
            )
    );
}
// Test 11: Cancel Order - Success
@Test
void cancelOrder_shouldCancelOrderAndRestoreStock() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    Product product = new Product();
    product.setId(1L);
    product.setName("Test Laptop");
    product.setPrice(50000);
    product.setQuantity(8);

    Order order = new Order();
    order.setId(1L);
    order.setUser(user);
    order.setProduct(product);
    order.setQuantity(2);
    order.setTotalPrice(100000);
    order.setStatus(OrderStatus.PLACED);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    when(orderRepository.save(any(Order.class)))
            .thenReturn(order);

    orderService.cancelOrder(1L, "test@example.com");

    assertEquals(OrderStatus.CANCELLED, order.getStatus());

    // Stock should increase from 8 to 10
    assertEquals(10, product.getQuantity());

    org.mockito.Mockito.verify(productRepository)
            .save(product);

    org.mockito.Mockito.verify(orderRepository)
            .save(order);
}
// Test 12: Cancel Order - Unauthorized User
@Test
void cancelOrder_shouldThrowExceptionWhenUserCancelsAnotherUsersOrder() {

    User loggedInUser = new User();
    loggedInUser.setId(1L);
    loggedInUser.setEmail("test@example.com");
    loggedInUser.setRole("USER");

    User orderOwner = new User();
    orderOwner.setId(2L);
    orderOwner.setEmail("owner@example.com");
    orderOwner.setRole("USER");

    Order order = new Order();
    order.setId(1L);
    order.setUser(orderOwner);
    order.setStatus(OrderStatus.PLACED);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(loggedInUser);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.cancelOrder(
                    1L,
                    "test@example.com"
            )
    );
}
// Test 13: Cancel Order - Already Cancelled
@Test
void cancelOrder_shouldThrowExceptionWhenOrderIsAlreadyCancelled() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    Order order = new Order();
    order.setId(1L);
    order.setUser(user);
    order.setStatus(OrderStatus.CANCELLED);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.cancelOrder(
                    1L,
                    "test@example.com"
            )
    );
}
// Test 14: Cancel Order - Delivered Order
@Test
void cancelOrder_shouldThrowExceptionWhenOrderIsDelivered() {

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRole("USER");

    Order order = new Order();
    order.setId(1L);
    order.setUser(user);
    order.setStatus(OrderStatus.DELIVERED);

    when(userService.findByEmail("test@example.com"))
            .thenReturn(user);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.cancelOrder(
                    1L,
                    "test@example.com"
            )
    );
}
// Test 15: Update Order Status - Success
@Test
void updateOrderStatus_shouldUpdateStatusSuccessfully() {

    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.PLACED);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    when(orderRepository.save(any(Order.class)))
            .thenReturn(order);

    Order response = orderService.updateOrderStatus(
            1L,
            OrderStatus.SHIPPED
    );

    assertEquals(OrderStatus.SHIPPED, response.getStatus());

    org.mockito.Mockito.verify(orderRepository)
            .save(order);
}
// Test 16: Update Order Status - Order Not Found
@Test
void updateOrderStatus_shouldThrowExceptionWhenOrderNotFound() {

    when(orderRepository.findById(99L))
            .thenReturn(java.util.Optional.empty());

    assertThrows(
            com.ecommerce.backend.exception.OrderNotFoundException.class,
            () -> orderService.updateOrderStatus(
                    99L,
                    OrderStatus.SHIPPED
            )
    );
}
// Test 17: Update Order Status - Cancelled Order
@Test
void updateOrderStatus_shouldThrowExceptionWhenOrderIsCancelled() {

    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.CANCELLED);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.updateOrderStatus(
                    1L,
                    OrderStatus.SHIPPED
            )
    );
}
// Test 18: Update Order Status - Delivered Order
@Test
void updateOrderStatus_shouldThrowExceptionWhenOrderIsDelivered() {

    Order order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.DELIVERED);

    when(orderRepository.findById(1L))
            .thenReturn(java.util.Optional.of(order));

    assertThrows(
            IllegalArgumentException.class,
            () -> orderService.updateOrderStatus(
                    1L,
                    OrderStatus.SHIPPED
            )
    );
}

}