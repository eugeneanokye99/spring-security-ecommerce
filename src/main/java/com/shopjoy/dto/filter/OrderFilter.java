package com.shopjoy.dto.filter;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderFilter {
    private String status;
    private String paymentStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double minAmount;
    private Double maxAmount;
    private String searchTerm;
}
