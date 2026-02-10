package com.shopjoy.graphql.resolver.mutation;

import com.shopjoy.dto.mapper.GraphQLMapperStruct;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.graphql.input.CreateUserInput;
import com.shopjoy.graphql.input.UpdateUserInput;
import com.shopjoy.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class UserMutationResolver {

    private final UserService userService;
    private final GraphQLMapperStruct graphQLMapper;

    @MutationMapping
    public UserResponse createUser(@Argument @Valid CreateUserInput input) {
        var request = graphQLMapper.toCreateUserRequest(input);
        return userService.registerUser(request);
    }

    @MutationMapping
    public UserResponse updateUser(@Argument Long id, @Argument @Valid UpdateUserInput input) {
        var request = graphQLMapper.toUpdateUserRequest(input);
        return userService.updateUserProfile(id.intValue(), request);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        userService.deleteUser(id.intValue());
        return true;
    }
}
