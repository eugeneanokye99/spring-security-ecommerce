package com.shopjoy.repository;

import com.shopjoy.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByProductId(int productId);
    List<Review> findByUserId(int userId);
    
    @Modifying
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.reviewId = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") int reviewId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRating(@Param("productId") int productId);
    
    boolean existsByUserIdAndProductId(int userId, int productId);
}
