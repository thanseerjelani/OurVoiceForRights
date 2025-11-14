package com.mgnrega.dashboard.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance", indexes = {
        @Index(name = "idx_district_year", columnList = "district_id, fin_year, month_name"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    @JsonIgnore
    private District district;

    @Column(name = "month_name", nullable = false)
    private String monthName;

    @Column(name = "fin_year", nullable = false)
    private String finYear;

    @Column(name = "total_households_worked")
    private Long totalHouseholdsWorked;

    @Column(name = "average_days_employment", precision = 10, scale = 2)
    private BigDecimal averageDaysEmployment;

    @Column(name = "total_wages", precision = 15, scale = 2)
    private BigDecimal totalWages;

    @Column(name = "ongoing_works")
    private Long ongoingWorks;

    @Column(name = "completed_works")
    private Long completedWorks;

    @Column(name = "total_expenditure", precision = 15, scale = 2)
    private BigDecimal totalExpenditure;

    @Column(name = "avg_wage_rate", precision = 10, scale = 2)
    private BigDecimal avgWageRate;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "data_source")
    private String dataSource;
}