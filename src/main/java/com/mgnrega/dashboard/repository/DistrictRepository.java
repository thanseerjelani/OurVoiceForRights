package com.mgnrega.dashboard.repository;

import com.mgnrega.dashboard.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    @Query("SELECT d FROM District d WHERE d.state.id = :stateId")
    List<District> findByStateId(@Param("stateId") Long stateId);

    Optional<District> findByDistrictCode(String districtCode);

    @Query("SELECT d FROM District d WHERE d.name = :name AND d.state.id = :stateId")
    Optional<District> findByNameAndStateId(@Param("name") String name, @Param("stateId") Long stateId);
}