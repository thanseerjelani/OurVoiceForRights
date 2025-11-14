package com.mgnrega.dashboard.repository;

import com.mgnrega.dashboard.entity.Performance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    @Query("SELECT p FROM Performance p WHERE p.district.id = :districtId ORDER BY p.finYear DESC, p.monthName DESC")
    Page<Performance> findByDistrictIdOrderByYearDescMonthDesc(
            @Param("districtId") Long districtId,
            Pageable pageable
    );

    @Query("SELECT p FROM Performance p WHERE p.district.id = :districtId AND p.finYear = :finYear ORDER BY p.monthName DESC")
    List<Performance> findByDistrictIdAndYear(
            @Param("districtId") Long districtId,
            @Param("finYear") String finYear
    );

    Optional<Performance> findTopByDistrictIdOrderByFinYearDescMonthNameDesc(Long districtId);

    @Query("SELECT p FROM Performance p WHERE p.district.id = :districtId AND p.finYear = :finYear AND p.monthName = :monthName")
    Optional<Performance> findByDistrictIdAndYearAndMonth(
            @Param("districtId") Long districtId,
            @Param("finYear") String finYear,
            @Param("monthName") String monthName
    );
}