package com.shopjoy.repository;

import com.shopjoy.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductId(int productId);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityInStock = :quantity WHERE i.productId = :productId")
    void updateStock(@Param("productId") int productId, @Param("quantity") int quantity);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityInStock = i.quantityInStock + :increment WHERE i.productId = :productId")
    void incrementStock(@Param("productId") int productId, @Param("increment") int increment);
    
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityInStock = i.quantityInStock - :decrement WHERE i.productId = :productId")
    void decrementStock(@Param("productId") int productId, @Param("decrement") int decrement);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantityInStock <= i.reorderLevel")
    List<Inventory> findLowStock();
    
    @Query("SELECT i FROM Inventory i WHERE i.productId IN :productIds")
    List<Inventory> findByProductIdIn(@Param("productIds") List<Integer> productIds);
}
