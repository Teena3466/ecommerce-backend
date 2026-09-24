package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // =========================================================
    // CREATE SINGLE PRODUCT ORDER
    // =========================================================

    @PostMapping
    public Order createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            Authentication authentication) {

        String email =
                authentication.getName();

        return orderService.createOrder(
                email,
                orderDTO.getProductId(),
                orderDTO.getQuantity()
        );
    }

    // =========================================================
    // CHECKOUT CART
    // =========================================================

    @PostMapping("/checkout")
    public List<Order> checkoutCart(
            Authentication authentication) {

        String email =
                authentication.getName();

        return orderService.checkoutCart(email);
    }

    // =========================================================
    // GET ALL ORDERS - ADMIN
    // =========================================================

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // =========================================================
    // GET MY ORDERS
    // =========================================================

    @GetMapping("/my-orders")
    public List<Order> getMyOrders(
            Authentication authentication) {

        String email =
                authentication.getName();

        return orderService.getMyOrders(email);
    }

    // =========================================================
    // GET ORDERS BY USER ID
    // =========================================================

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(
            @PathVariable Long userId,
            Authentication authentication) {

        String email =
                authentication.getName();

        return orderService.getOrdersByUser(
                userId,
                email
        );
    }

    // =========================================================
    // CANCEL ORDER
    // =========================================================

    @DeleteMapping("/{orderId}")
    public String cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        String email =
                authentication.getName();

        orderService.cancelOrder(
                orderId,
                email
        );

        return "Order cancelled successfully";
    }

    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @PutMapping("/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {

        return orderService.updateOrderStatus(
                orderId,
                status
        );
    }
}