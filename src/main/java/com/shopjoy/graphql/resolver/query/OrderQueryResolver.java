package com.shopjoy.graphql.resolver.query;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.graphql.input.OrderFilterInput;
import com.shopjoy.graphql.type.OrderConnection;
import com.shopjoy.graphql.type.PageInfo;
import com.shopjoy.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;


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
            @Argument OrderFilterInput filter,
            @Argument Integer page,
            @Argument Integer size,
            @Argument String sortBy,
            @Argument String sortDirection
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        
        String sortField = sortBy != null ? sortBy : "orderDate";
        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(direction, sortField));

        OrderFilter orderFilter = null;
        if (filter != null) {
            orderFilter = OrderFilter.builder()
                    .status(filter.status())
                    .paymentStatus(filter.paymentStatus())
                    .startDate(filter.startDate())
                    .endDate(filter.endDate())
                    .minAmount(filter.minAmount())
                    .maxAmount(filter.maxAmount())
                    .searchTerm(filter.searchTerm())
                    .build();
        }

        Integer uid = userId != null ? userId.intValue() : null;
        Page<OrderResponse> orderPage = orderService.getOrders(uid, orderFilter, pageable);
        
        PageInfo pageInfo = new PageInfo(
                pageNum,
                pageSize,
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );

        return new OrderConnection(orderPage.getContent(), pageInfo);
    }
}
