package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.graphql.type.OrderConnection;
import com.shopjoy.graphql.type.PageInfo;
import com.shopjoy.service.OrderService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class OrderQueryResolver {

    private final OrderService orderService;

    public OrderQueryResolver(OrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    public OrderResponse order(@Argument Long id) {
        return orderService.getOrderById(id.intValue());
    }

    @QueryMapping
    public OrderConnection orders(
            @Argument Long userId,
            @Argument Integer page,
            @Argument Integer size
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        // Service doesn't support pagination, so get all and paginate manually
        List<OrderResponse> allOrders;
        if (userId != null) {
            allOrders = orderService.getOrdersByUser(userId.intValue());
        } else {
            // Get all orders for dashboard analytics
            allOrders = orderService.getAllOrders();
        }
        
        int start = pageNum * pageSize;
        int end = Math.min(start + pageSize, allOrders.size());
        List<OrderResponse> paginatedOrders = (start < allOrders.size()) 
            ? allOrders.subList(start, end) 
            : List.of();
        
        int totalElements = allOrders.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        PageInfo pageInfo = new PageInfo(
                pageNum,
                pageSize,
                totalElements,
                totalPages
        );

        return new OrderConnection(paginatedOrders, pageInfo);
    }
}
