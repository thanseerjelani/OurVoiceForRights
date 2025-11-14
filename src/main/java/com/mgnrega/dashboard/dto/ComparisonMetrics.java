package com.mgnrega.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonMetrics {
    private BigDecimal householdsChange;
    private BigDecimal daysWorkedChange;
    private BigDecimal wagesChange;
    private Integer projectsChange;
}
