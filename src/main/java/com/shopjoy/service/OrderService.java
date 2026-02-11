package com.shopjoy.service;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.exception.InsufficientStockException;
import com.shopjoy.exception.InvalidOrderStateException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Order-related business operations.
 * Handles order creation, status management, and complex order workflows.
 */
public interface OrderService {

    /**
     * Creates a new order for a user.
     * Validates product availability, updates inventory, and calculates total.
     * This is a complex transaction involving multiple entities.
     * 
     * @param request the create order request
     * @return the created order response
     * @throws ResourceNotFoundException  if user or products not found
     * @throws InsufficientStockException if any product is out of stock
     * @throws ValidationException        if order data is invalid
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Processes payment for an existing order.
     * Updates payment status and potentially triggers order status transition.
     * 
     * @param orderId the order ID
     * @param transactionId external payment gateway transaction ID
     * @return the updated order response
     * @throws ResourceNotFoundException if order not found
     * @throws InvalidOrderStateException if order is already paid or cancelled
     */
    OrderResponse processPayment(Integer orderId, String transactionId);

    /**
     * Simulates payment for demonstration purposes.
     * Includes logic to trigger a transactional rollback under certain conditions.
     * 
     * @param orderId the order ID
     * @param transactionId the transaction ID
     * @return the updated order response
     */
    OrderResponse simulatePayment(Integer orderId, String transactionId);

    /**
     * Retrieves an order by its ID.
     */
    OrderResponse getOrderById(Integer orderId);

    /**
     * Retrieves all orders for a specific user.
     * 
     * @param userId the user ID
     * @return list of user's order responses
     */
    List<OrderResponse> getOrdersByUser(Integer userId);

    /**
     * Retrieves orders for a user with pagination.
     * 
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return paginated order responses
     */
    Page<OrderResponse> getOrdersByUserPaginated(Integer userId, Pageable pageable);

    /**
     * Retrieves all orders with a specific status.
     * 
     * @param status the order status
     * @return list of order responses with the status
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    /**
     * Retrieves orders by status with pagination.
     * 
     * @param status order status
     * @param pageable pagination parameters
     * @return paginated order responses
     */
    Page<OrderResponse> getOrdersByStatusPaginated(OrderStatus status, Pageable pageable);

    /**
     * Retrieves orders within a date range.
     * 
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of order responses in the date range
     */
    List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves orders by date range with pagination.
     * 
     * @param startDate start date
     * @param endDate end date
     * @param pageable pagination parameters
     * @return paginated order responses
     */
    Page<OrderResponse> getOrdersByDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Retrieves orders based on a variety of filters.
     * 
     * @param userId   optional user ID to filter by
     * @param filter   the filter criteria
     * @param pageable pagination and sorting parameters
     * @return paginated order responses
     */
    Page<OrderResponse> getOrders(Integer userId, OrderFilter filter, Pageable pageable);

    /**
     * Retrieves all orders with pagination.
     * 
     * @param pageable pagination parameters
     * @return paginated order responses
     */
    Page<OrderResponse> getAllOrdersPaginated(Pageable pageable);

    /**
     * Updates an order's status.
     * Validates that the status transition is allowed.
     * 
     * @param orderId   the order ID
     * @param newStatus the new status
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if status transition is invalid
     */
    OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus);

    /**
     * Confirms a pending order (moves to CONFIRMED status).
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order is not pending
     */
    OrderResponse confirmOrder(Integer orderId);

    /**
     * Ships an order (moves to SHIPPED status).
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order is not confirmed
     */
    OrderResponse shipOrder(Integer orderId);

    /**
     * Completes an order (moves to COMPLETED status).
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order is not shipped
     */
    OrderResponse completeOrder(Integer orderId);

    /**
     * Cancels an order and restores inventory.
     * Can only cancel pending or confirmed orders.
     * 
     * @param orderId the order ID
     * @return the updated order response
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if order cannot be cancelled
     */
    OrderResponse cancelOrder(Integer orderId);

    /**
     * Retrieves pending orders (requires action).
     * 
     * @return list of pending order responses
     */
    /**
     * Retrieves pending orders (requires action).
     * 
     * @return list of pending order responses
     */
    List<OrderResponse> getPendingOrders();

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request);

    void deleteOrder(Integer orderId);
}
