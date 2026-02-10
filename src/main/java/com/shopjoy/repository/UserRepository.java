package com.shopjoy.repository;

import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.passwordHash = :password")
    Optional<User> authenticate(@Param("username") String username, @Param("password") String password);
    
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByUserType(UserType userType);
    
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :newPassword WHERE u.userId = :userId")
    void changePassword(@Param("userId") int userId, @Param("newPassword") String newPassword);
}
