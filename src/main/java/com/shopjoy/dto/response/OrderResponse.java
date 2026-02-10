package com.shopjoy.dto.response;

import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order response containing order details, status, and payment information")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    @Schema(description = "Order unique identifier", example = "1")
    private Integer orderId;

    @Schema(description = "User ID who placed the order", example = "1")
    private Integer userId;

    @Schema(description = "User's full name", example = "John Doe")
    private String userName;

    @Schema(description = "Order placement timestamp", example = "2024-01-20T10:30:00")
    private LocalDateTime orderDate;

    @Schema(description = "Total order amount including all items", example = "1299.99")
    private double totalAmount;

    @Schema(description = "Current order status", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Shipping address for delivery", example = "123 Main St, Apt 4B, New York, NY 10001")
    private String shippingAddress;

    @Schema(description = "Payment method used", example = "Credit Card")
    private String paymentMethod;

    @Schema(description = "Payment processing status", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Additional order notes", example = "Please call before delivery")
    private String notes;

    @Schema(description = "List of items in the order")
    private List<OrderItemResponse> orderItems;

    @Schema(description = "Order creation timestamp", example = "2024-01-20T10:30:00")
    private LocalDateTime createdAt;

}
