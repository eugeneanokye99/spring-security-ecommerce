package com.shopjoy.repository;

import com.shopjoy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    @Query("SELECT c FROM Category c WHERE c.parentCategoryId IS NULL")
    List<Category> findTopLevelCategories();
    
    @Query("SELECT c FROM Category c WHERE c.parentCategoryId = :parentId")
    List<Category> findSubcategories(@Param("parentId") Integer parentCategoryId);
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parentCategoryId = :categoryId")
    boolean hasSubcategories(@Param("categoryId") int categoryId);
}
