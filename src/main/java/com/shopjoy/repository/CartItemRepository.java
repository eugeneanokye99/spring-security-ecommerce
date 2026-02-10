package com.shopjoy.repository;

import com.shopjoy.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserId(int userId);
    
    @Query("SELECT c FROM CartItem c WHERE c.userId = :userId AND c.productId = :productId")
    Optional<CartItem> findByUserAndProduct(@Param("userId") int userId, @Param("productId") int productId);
    
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId")
    void clearCart(@Param("userId") int userId);
}
