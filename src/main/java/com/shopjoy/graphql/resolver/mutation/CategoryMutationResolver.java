package com.shopjoy.graphql.resolver.mutation;

import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.graphql.input.CreateCategoryInput;
import com.shopjoy.graphql.input.UpdateCategoryInput;
import com.shopjoy.dto.mapper.GraphQLMapperStruct;
import com.shopjoy.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class CategoryMutationResolver {

    private final CategoryService categoryService;
    private final GraphQLMapperStruct graphQLMapper;

    @MutationMapping
    public CategoryResponse createCategory(@Argument @Valid CreateCategoryInput input) {
        var request = graphQLMapper.toCreateCategoryRequest(input);
        return categoryService.createCategory(request);
    }

    @MutationMapping
    public CategoryResponse updateCategory(@Argument Long id, @Argument @Valid UpdateCategoryInput input) {
        var request = graphQLMapper.toUpdateCategoryRequest(input);
        return categoryService.updateCategory(id.intValue(), request);
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument Long id) {
        categoryService.deleteCategory(id.intValue());
        return true;
    }
}
