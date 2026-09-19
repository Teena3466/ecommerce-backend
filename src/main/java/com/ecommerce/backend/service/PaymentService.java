package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.PaymentDTO;
import com.ecommerce.backend.dto.PaymentResponseDTO;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderStatus;
import com.ecommerce.backend.entity.Payment;
import com.ecommerce.backend.entity.PaymentStatus;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.PaymentRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserService userService) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    // Make Payment
    public PaymentResponseDTO makePayment(
            String email,
            PaymentDTO paymentDTO) {

        // Get logged-in user
        User user = userService.findByEmail(email);

        // Find order
        Order order = orderRepository
                .findById(paymentDTO.getOrderId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found"));

        // Check ownership
        if (!user.getId().equals(order.getUser().getId())) {
            throw new IllegalArgumentException(
                    "You can only pay for your own order");
        }

        // Payment is allowed only for PLACED orders
        if (!OrderStatus.PLACED.equals(order.getStatus())) {
            throw new IllegalArgumentException(
                    "Payment can only be made for a placed order");
        }

        // Prevent duplicate payment
        if (paymentRepository
                .findByOrderId(order.getId())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Payment already exists for this order");
        }

        // Create payment
        Payment payment = new Payment();

        payment.setUserId(user.getId());
        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalPrice());
        payment.setMethod(paymentDTO.getMethod());
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment =
                paymentRepository.save(payment);

        return convertToResponse(savedPayment);
    }

    // Get My Payments
    public List<PaymentResponseDTO> getMyPayments(
            String email) {

        User user = userService.findByEmail(email);

        return paymentRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get Payment By ID
    public PaymentResponseDTO getPayment(
            String email,
            Long paymentId) {

        User user = userService.findByEmail(email);

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found"));

        // Check ownership
        if (!user.getId().equals(payment.getUserId())) {
            throw new IllegalArgumentException(
                    "You can only access your own payment");
        }

        return convertToResponse(payment);
    }

    // Convert Entity to Response DTO
    private PaymentResponseDTO convertToResponse(
            Payment payment) {

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod()
        );
    }
}