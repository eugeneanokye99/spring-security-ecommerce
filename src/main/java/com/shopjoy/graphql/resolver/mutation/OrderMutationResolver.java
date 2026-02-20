package com.shopjoy.graphql.resolver.mutation;

import com.shopjoy.dto.mapper.GraphQLMapperStruct;
import com.shopjoy.dto.response.OrderResponse;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.graphql.input.UpdateOrderInput;
import com.shopjoy.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class OrderMutationResolver {

    private final OrderService orderService;
    private final GraphQLMapperStruct graphQLMapper;

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderResponse updateOrder(@Argument Long id, @Argument @Valid UpdateOrderInput input) {
        var request = graphQLMapper.toUpdateOrderRequest(input);
        return orderService.updateOrder(id.intValue(), request);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteOrder(@Argument Long id) {
        orderService.deleteOrder(id.intValue());
        return true;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderResponse cancelOrder(@Argument Long id) {
        return orderService.cancelOrder(id.intValue());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse updateOrderStatus(@Argument Long id, @Argument String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        return orderService.updateOrderStatus(id.intValue(), orderStatus);
    }
}
