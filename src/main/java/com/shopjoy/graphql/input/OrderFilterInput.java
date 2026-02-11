package com.shopjoy.graphql.input;

import java.time.LocalDateTime;

public record OrderFilterInput(
    String status,
    String paymentStatus,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Double minAmount,
    Double maxAmount,
    String searchTerm
) {}
