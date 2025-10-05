package com.borsibaar.backend.dto;

import java.math.BigDecimal;

public record UserSalesStatsResponseDto(
        String userId,
        String userName,
        String userEmail,
        Long salesCount,
        BigDecimal totalRevenue
) {}