package com.mgnrega.dashboard.repository;

import com.mgnrega.dashboard.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByName(String name);
    Optional<State> findByStateCode(String stateCode);
}