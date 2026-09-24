package com.ecommerce.backend.service;

import com.ecommerce.backend.entity.Cart;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.CartRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.exception.OrderNotFoundException;
import com.ecommerce.backend.exception.ProductNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final CartRepository cartRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserService userService,
            CartRepository cartRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.cartRepository = cartRepository;
    }

    // =========================================================
    // CREATE SINGLE PRODUCT ORDER
    // =========================================================

    @Transactional
    public Order createOrder(
            String email,
            Long productId,
            int quantity) {

        logger.info(
                "Creating order - email: {}, productId: {}, quantity: {}",
                email,
                productId,
                quantity
        );

        if (productId == null) {
            logger.warn("Order creation failed. Product ID is null");
            throw new IllegalArgumentException(
                    "Product ID is required"
            );
        }

        if (quantity <= 0) {
            logger.warn(
                    "Order creation failed. Invalid quantity: {}",
                    quantity
            );

            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        User user = userService.findByEmail(email);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn(
                            "Order creation failed. Product not found: {}",
                            productId
                    );

                    return new ProductNotFoundException(
                            "Product not found"
                    );
                });

        if (product.getQuantity() < quantity) {
            logger.warn(
                    "Order creation failed. Insufficient stock for product: {}",
                    productId
            );

            throw new IllegalArgumentException(
                    "Insufficient stock"
            );
        }

        double totalPrice =
                product.getPrice() * quantity;

        product.setQuantity(
                product.getQuantity() - quantity
        );

        productRepository.save(product);

        Order order = new Order();

        order.setUser(user);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder =
                orderRepository.save(order);

        logger.info(
                "Order created successfully with id: {}",
                savedOrder.getId()
        );

        return savedOrder;
    }

    // =========================================================
    // CHECKOUT CART
    // =========================================================

    @Transactional
    public List<Order> checkoutCart(String email) {

        logger.info(
                "Starting cart checkout for user: {}",
                email
        );

        User user = userService.findByEmail(email);

        List<Cart> cartItems =
                cartRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            logger.warn(
                    "Checkout failed. Cart is empty for user: {}",
                    email
            );

            throw new IllegalArgumentException(
                    "Cart is empty"
            );
        }

        // Check stock first

        for (Cart cart : cartItems) {

            Product product =
                    productRepository
                            .findById(cart.getProductId())
                            .orElseThrow(() -> {

                                logger.warn(
                                        "Checkout failed. Product not found: {}",
                                        cart.getProductId()
                                );

                                return new ProductNotFoundException(
                                        "Product not found"
                                );
                            });

            if (product.getQuantity()
                    < cart.getQuantity()) {

                logger.warn(
                        "Checkout failed. Insufficient stock for product: {}",
                        product.getName()
                );

                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }
        }

        List<Order> orders =
                new ArrayList<>();

        // Create orders

        for (Cart cart : cartItems) {

            Product product =
                    productRepository
                            .findById(cart.getProductId())
                            .orElseThrow(() ->
                                    new ProductNotFoundException(
                                            "Product not found"
                                    )
                            );

            double totalPrice =
                    product.getPrice()
                            * cart.getQuantity();

            product.setQuantity(
                    product.getQuantity()
                            - cart.getQuantity()
            );

            productRepository.save(product);

            Order order = new Order();

            order.setUser(user);
            order.setProduct(product);
            order.setQuantity(cart.getQuantity());
            order.setTotalPrice(totalPrice);
            order.setStatus(OrderStatus.PLACED);
            order.setCreatedAt(LocalDateTime.now());

            Order savedOrder =
                    orderRepository.save(order);

            orders.add(savedOrder);

            logger.info(
                    "Order created from cart with id: {}",
                    savedOrder.getId()
            );
        }

        // Clear cart

        cartRepository.deleteAll(cartItems);

        logger.info(
                "Cart checkout completed successfully for user: {}",
                email
        );

        return orders;
    }

    // =========================================================
    // GET ALL ORDERS - ADMIN
    // =========================================================

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {

        logger.info("Fetching all orders");

        List<Order> orders =
                orderRepository.findAll();

        logger.info(
                "Found {} total orders",
                orders.size()
        );

        return orders;
    }

    // =========================================================
    // GET MY ORDERS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(String email) {

        logger.info(
                "Fetching orders for logged-in user: {}",
                email
        );

        User user =
                userService.findByEmail(email);

        List<Order> orders =
                orderRepository.findByUserId(
                        user.getId()
                );

        logger.info(
                "Found {} orders for user: {}",
                orders.size(),
                email
        );

        return orders;
    }

    // =========================================================
    // GET ORDERS BY USER ID
    // =========================================================

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(
            Long userId,
            String email) {

        logger.info(
                "Fetching orders for userId: {} by email: {}",
                userId,
                email
        );

        User loggedInUser =
                userService.findByEmail(email);

        if ("USER".equals(loggedInUser.getRole())
                && !loggedInUser.getId().equals(userId)) {

            logger.warn(
                    "Unauthorized order access attempt by user: {}",
                    email
            );

            throw new IllegalArgumentException(
                    "You can only view your own orders"
            );
        }

        return orderRepository.findByUserId(userId);
    }

    // =========================================================
    // CANCEL ORDER
    // =========================================================

    @Transactional
    public void cancelOrder(
            Long orderId,
            String email) {

        logger.info(
                "Cancelling order: {} by user: {}",
                orderId,
                email
        );

        User loggedInUser =
                userService.findByEmail(email);

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Cancel failed. Order not found: {}",
                                    orderId
                            );

                            return new OrderNotFoundException(
                                    "Order not found"
                            );
                        });

        if ("USER".equals(loggedInUser.getRole())
                && !loggedInUser.getId()
                        .equals(order.getUser().getId())) {

            logger.warn(
                    "Unauthorized cancellation attempt for order: {}",
                    orderId
            );

            throw new IllegalArgumentException(
                    "You can only cancel your own orders"
            );
        }

        if (OrderStatus.CANCELLED
                .equals(order.getStatus())) {

            logger.warn(
                    "Order {} is already cancelled",
                    orderId
            );

            throw new IllegalArgumentException(
                    "Order is already cancelled"
            );
        }

        if (OrderStatus.DELIVERED
                .equals(order.getStatus())) {

            logger.warn(
                    "Cannot cancel delivered order: {}",
                    orderId
            );

            throw new IllegalArgumentException(
                    "Delivered order cannot be cancelled"
            );
        }

        Product product =
                order.getProduct();

        if (product != null) {

            product.setQuantity(
                    product.getQuantity()
                            + order.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(
                OrderStatus.CANCELLED
        );

        orderRepository.save(order);

        logger.info(
                "Order cancelled successfully: {}",
                orderId
        );
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Transactional
    public Order updateOrderStatus(
            Long orderId,
            OrderStatus status) {

        logger.info(
                "Updating order {} status to {}",
                orderId,
                status
        );

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() -> {

                            logger.warn(
                                    "Status update failed. Order not found: {}",
                                    orderId
                            );

                            return new OrderNotFoundException(
                                    "Order not found"
                            );
                        });

        if (OrderStatus.CANCELLED
                .equals(order.getStatus())) {

            logger.warn(
                    "Cannot update status of cancelled order: {}",
                    orderId
            );

            throw new IllegalArgumentException(
                    "Cancelled order status cannot be changed"
            );
        }

        if (OrderStatus.DELIVERED
                .equals(order.getStatus())) {

            logger.warn(
                    "Cannot update status of delivered order: {}",
                    orderId
            );

            throw new IllegalArgumentException(
                    "Delivered order status cannot be changed"
            );
        }

        order.setStatus(status);

        Order updatedOrder =
                orderRepository.save(order);

        logger.info(
                "Order status updated successfully. Order: {}, status: {}",
                orderId,
                status
        );

        return updatedOrder;
    }
}