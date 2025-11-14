package com.mgnrega.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Performance Response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResponse {
    private Long id;
    private Long districtId;
    private String districtName;
    private String monthName;
    private String finYear;
    private Long totalHouseholdsWorked;
    private BigDecimal averageDaysEmployment;
    private BigDecimal totalWages;
    private Long ongoingWorks;
    private Long completedWorks;
    private BigDecimal totalExpenditure;
    private BigDecimal avgWageRate;
    private LocalDateTime lastUpdated;
    private String trend;
    private String performanceLevel;
}
