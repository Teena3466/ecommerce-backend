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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentService.class);

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

        logger.info(
                "Processing payment for user: {}, orderId: {}",
                email,
                paymentDTO.getOrderId());

        User user = userService.findByEmail(email);

        Order order = orderRepository
                .findById(paymentDTO.getOrderId())
                .orElseThrow(() -> {

                    logger.warn(
                            "Payment failed. Order not found: {}",
                            paymentDTO.getOrderId());

                    return new IllegalArgumentException(
                            "Order not found");
                });

        // Check ownership
        if (!user.getId().equals(order.getUser().getId())) {

            logger.warn(
                    "Unauthorized payment attempt. User: {}, orderId: {}",
                    email,
                    order.getId());

            throw new IllegalArgumentException(
                    "You can only pay for your own order");
        }

        // Payment is allowed only for PLACED orders
        if (!OrderStatus.PLACED.equals(order.getStatus())) {

            logger.warn(
                    "Payment failed. Order {} has status: {}",
                    order.getId(),
                    order.getStatus());

            throw new IllegalArgumentException(
                    "Payment can only be made for a placed order");
        }

        // Prevent duplicate payment
        if (paymentRepository
                .findByOrderId(order.getId())
                .isPresent()) {

            logger.warn(
                    "Payment already exists for order: {}",
                    order.getId());

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

        logger.info(
                "Payment successful. PaymentId: {}, orderId: {}",
                savedPayment.getId(),
                order.getId());

        return convertToResponse(savedPayment);
    }

    // Get My Payments
    public List<PaymentResponseDTO> getMyPayments(
            String email) {

        logger.info(
                "Fetching payments for user: {}",
                email);

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

        logger.info(
                "Fetching payment {} for user: {}",
                paymentId,
                email);

        User user = userService.findByEmail(email);

        Payment payment = paymentRepository
                .findById(paymentId)
                .orElseThrow(() -> {

                    logger.warn(
                            "Payment not found: {}",
                            paymentId);

                    return new IllegalArgumentException(
                            "Payment not found");
                });

        // Check ownership
        if (!user.getId().equals(payment.getUserId())) {

            logger.warn(
                    "Unauthorized payment access. User: {}, paymentId: {}",
                    email,
                    paymentId);

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