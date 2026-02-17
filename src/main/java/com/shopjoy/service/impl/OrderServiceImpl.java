package com.shopjoy.service.impl;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.mapper.OrderMapperStruct;
import com.shopjoy.dto.request.CreateOrderItemRequest;
import com.shopjoy.dto.request.CreateOrderRequest;
import com.shopjoy.dto.request.UpdateOrderItemRequest;
import com.shopjoy.dto.request.UpdateOrderRequest;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.dto.response.ProductResponse;
import com.shopjoy.entity.Order;
import com.shopjoy.entity.OrderItem;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import com.shopjoy.exception.InvalidOrderStateException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.OrderItemRepository;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.specification.OrderSpecification;
import com.shopjoy.service.InventoryService;
import com.shopjoy.service.OrderService;
import com.shopjoy.service.ProductService;
import com.shopjoy.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Order service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final ProductService productService;
    private final UserService userService;
    private final OrderMapperStruct orderMapper;


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        userService.getUserById(request.getUserId());

        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new ValidationException("Shipping address is required");
        }

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }

        Map<Integer, ProductResponse> productCache = new HashMap<>();
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productService.getProductById(itemReq.getProductId());
            productCache.put(itemReq.getProductId(), product);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productCache.get(itemReq.getProductId());
            if (!product.isActive()) {
                throw new ValidationException("Product " + product.getProductName() + " is not active");
            }
            if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                throw new ValidationException("Insufficient stock for product: " + product.getProductName());
            }
            BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity());
        }

        Order order = orderMapper.toOrder(request);
        order.setUser(userRepository.getReferenceById(request.getUserId()));
        order.setTotalAmount(totalAmount);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        
        if (order.getPaymentMethod() == null || order.getPaymentMethod().trim().isEmpty()) {
            order.setPaymentMethod("CASH");
        }
        
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order createdOrder = orderRepository.save(order);

        for (CreateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productCache.get(itemReq.getProductId());
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            
            OrderItem orderItem = OrderItem.builder()
                    .order(createdOrder)
                    .product(productRepository.getReferenceById(itemReq.getProductId()))
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(orderItem);
        }

        return getOrderById(createdOrder.getId());
    }

    @Override
    public Page<OrderResponse> getOrders(Integer userId, OrderFilter filter, Pageable pageable) {
        Specification<Order> spec = OrderSpecification.withFilters(userId, filter);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        List<Order> orders = orderRepository.findByUser_Id(userId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getOrdersByUserPaginated(Integer userId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByUser_Id(userId, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            throw new ValidationException("Order status cannot be null");
        }
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getOrdersByStatusPaginated(OrderStatus status, Pageable pageable) {
        if (status == null) {
            throw new ValidationException("Order status cannot be null");
        }
        Page<Order> orderPage = orderRepository.findByStatus(status, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getOrdersByDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }
        Page<Order> orderPage = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    @Override
    public Page<OrderResponse> getAllOrdersPaginated(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
                
        return new PageImpl<>(content, pageable, orderPage.getTotalElements());
    }

    /**
     * STATE MACHINE PATTERN EXAMPLE
     * <p>
     * Order status transitions follow a specific workflow:
     * PENDING -> PROCESSING -> SHIPPED -> DELIVERED
     * -> CANCELLED (from PENDING or PROCESSING only)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse confirmOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "confirm");
        }

        return updateOrderStatus(orderId, OrderStatus.PROCESSING);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse shipOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "ship");
        }

        return updateOrderStatus(orderId, OrderStatus.SHIPPED);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse completeOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "complete");
        }

        return updateOrderStatus(orderId, OrderStatus.DELIVERED);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidOrderStateException(
                    orderId,
                    order.getStatus().toString(),
                    "cancel (can only cancel PENDING or PROCESSING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);

        for (OrderItem item : orderItems) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(cancelledOrder);
    }

    @Override
    public List<OrderResponse> getPendingOrders() {
        return getOrdersByStatus(OrderStatus.PENDING);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

@Transactional(isolation = Isolation.SERIALIZABLE)
public OrderResponse updateOrder(Integer orderId, UpdateOrderRequest request) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

    if (order.getStatus() != OrderStatus.PENDING) {
        throw new InvalidOrderStateException(orderId, order.getStatus().toString(), 
            "update (can only update PENDING orders)");
    }

    if (request.getShippingAddress() != null && !request.getShippingAddress().trim().isEmpty()) {
        order.setShippingAddress(request.getShippingAddress());
    }

    if (request.getPaymentMethod() != null) {
        order.setPaymentMethod(request.getPaymentMethod());
    }

    if (request.getNotes() != null) {
        order.setNotes(request.getNotes());
    }

    if (request.getOrderItems() != null && !request.getOrderItems().isEmpty()) {
        List<OrderItem> existingItems = orderItemRepository.findByOrder_Id(orderId);
        
        for (OrderItem item : existingItems) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
        }

        for (OrderItem item : existingItems) {
            orderItemRepository.deleteById(item.getId());
        }

        double newTotal = 0.0;
        for (UpdateOrderItemRequest itemReq : request.getOrderItems()) {
            ProductResponse product = productService.getProductById(itemReq.getProductId());
            if (!product.isActive()) {
                throw new ValidationException("Product " + product.getProductName() + " is not active");
            }
            if (!inventoryService.hasAvailableStock(itemReq.getProductId(), itemReq.getQuantity())) {
                throw new ValidationException("Insufficient stock for product: " + product.getProductName());
            }
            inventoryService.reserveStock(itemReq.getProductId(), itemReq.getQuantity());
            newTotal += itemReq.getPrice() * itemReq.getQuantity();
        }

        for (UpdateOrderItemRequest itemReq : request.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(productRepository.getReferenceById(itemReq.getProductId()))
                    .quantity(itemReq.getQuantity())
                    .unitPrice(new BigDecimal(itemReq.getPrice().toString()))
                    .subtotal(new BigDecimal(itemReq.getQuantity() * itemReq.getPrice()))
                    .createdAt(LocalDateTime.now())
                    .build();
            orderItemRepository.save(orderItem);
        }
        order.setTotalAmount(new BigDecimal(newTotal));
    }
    order.setUpdatedAt(LocalDateTime.now());
    Order updatedOrder = orderRepository.save(order);
    
    return orderMapper.toOrderResponse(updatedOrder);
}


    @Override
    @Transactional()
    public void deleteOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(orderId, order.getStatus().toString(), "delete (can only delete PENDING orders)");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);
        for (OrderItem item : orderItems) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
        }

        orderRepository.deleteById(orderId);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED || currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    0,
                    currentStatus.toString(),
                    "Cannot change status of cancelled or delivered orders");
        }

        switch (newStatus) {
            case PENDING:
                // PENDING is the initial state, no validation needed
                break;
            case PROCESSING:
                if (currentStatus != OrderStatus.PENDING) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only process PENDING orders");
                }
                break;
            case SHIPPED:
                if (currentStatus != OrderStatus.PROCESSING) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only ship PROCESSING orders");
                }
                break;
            case DELIVERED:
                if (currentStatus != OrderStatus.SHIPPED) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only deliver SHIPPED orders");
                }
                break;
            case CANCELLED:
                if (currentStatus != OrderStatus.PENDING && currentStatus != OrderStatus.PROCESSING) {
                    throw new InvalidOrderStateException(
                            0, currentStatus.toString(),
                            "Can only cancel PENDING or PROCESSING orders");
                }
                break;
        }
    }


    /**
     * PAYMENT WORKFLOW DEMONSTRATION
     * <p>
     * This method simulates a payment confirmation workflow.
     * 1. Updates payment status Atomically.
     * 2. Transitions order status to PROCESSING.
     * 3. Uses REQUIRED propagation to ensure both updates happen in one transaction.
     * <p>
     * If transitioning the order status fails (e.g., business rule violation), 
     * the payment status update will also be rolled back.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse processPayment(Integer orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Order is already paid");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(orderId, "CANCELLED", "process payment");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        
        orderRepository.save(order);

        return confirmOrder(orderId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderResponse simulatePayment(Integer orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ValidationException("Simulation: Order " + orderId + " is already paid.");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        if (transactionId != null && transactionId.startsWith("FAIL-")) {
            throw new RuntimeException("Simulated payment gateway failure for order " + orderId);
        }
        return confirmOrder(orderId);
    }
}
