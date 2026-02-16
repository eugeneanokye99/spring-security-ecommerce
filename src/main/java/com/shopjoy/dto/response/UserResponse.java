package com.shopjoy.dto.response;

import com.shopjoy.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "User response containing user profile details (password never included)")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    @Schema(description = "User unique identifier", example = "1")
    private Integer id;
    
    @Schema(description = "Username for login", example = "johndoe123")
    private String username;
    
    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User first name", example = "John")
    private String firstName;
    
    @Schema(description = "User last name", example = "Doe")
    private String lastName;
    
    @Schema(description = "User phone number", example = "+1234567890")
    private String phone;
    
    @Schema(description = "User account type", example = "CUSTOMER")
    private UserType userType;
    
    @Schema(description = "Account creation timestamp", example = "2024-01-20T10:30:00")
    private LocalDateTime createdAt;

}
