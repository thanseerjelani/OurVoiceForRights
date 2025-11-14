package com.mgnrega.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Comparison Response
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResponse {
    private PerformanceResponse current;
    private PerformanceResponse previous;
    private ComparisonMetrics comparison;
}
