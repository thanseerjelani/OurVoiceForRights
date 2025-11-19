package com.mgnrega.dashboard.controller;

import com.mgnrega.dashboard.dto.*;
import com.mgnrega.dashboard.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "https://voicesforrights.netlify.app")
public class MgnregaController {

    private final PerformanceService performanceService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Service is running"));
    }

    @GetMapping("/states")
    public ResponseEntity<ApiResponse<List<StateResponse>>> getAllStates() {
        log.info("Fetching all states");
        try {
            List<StateResponse> states = performanceService.getAllStates();
            return ResponseEntity.ok(ApiResponse.success(states));
        } catch (Exception e) {
            log.error("Error fetching states: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error fetching states: " + e.getMessage()));
        }
    }

    @GetMapping("/districts/{stateId}")
    public ResponseEntity<ApiResponse<List<DistrictResponse>>> getDistrictsByState(
            @PathVariable Long stateId) {
        log.info("Fetching districts for state ID: {}", stateId);
        try {
            List<DistrictResponse> districts = performanceService.getDistrictsByState(stateId);
            return ResponseEntity.ok(ApiResponse.success(districts));
        } catch (Exception e) {
            log.error("Error fetching districts: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error fetching districts: " + e.getMessage()));
        }
    }

    @GetMapping("/performance/{districtId}")
    public ResponseEntity<ApiResponse<PerformanceResponse>> getPerformance(
            @PathVariable Long districtId) {
        log.info("Fetching performance for district ID: {}", districtId);
        try {
            PerformanceResponse performance = performanceService.getLatestPerformance(districtId);

            if (performance == null) {
                return ResponseEntity.ok(ApiResponse.error("No performance data available"));
            }

            return ResponseEntity.ok(ApiResponse.success(performance));
        } catch (Exception e) {
            log.error("Error fetching performance: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error fetching performance: " + e.getMessage()));
        }
    }

    @GetMapping("/compare/{districtId}")
    public ResponseEntity<ApiResponse<ComparisonResponse>> getComparison(
            @PathVariable Long districtId,
            @RequestParam(required = false, defaultValue = "2024-2025") String year) {
        log.info("Fetching comparison for district ID: {}, year: {}", districtId, year);
        try {
            ComparisonResponse comparison = performanceService.getComparison(districtId, year);

            if (comparison == null) {
                return ResponseEntity.ok(ApiResponse.error("No comparison data available"));
            }

            return ResponseEntity.ok(ApiResponse.success(comparison));
        } catch (Exception e) {
            log.error("Error fetching comparison: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error fetching comparison: " + e.getMessage()));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> manualSync() {
        log.info("Manual sync triggered");
        try {
            performanceService.syncDataFromApi();
            return ResponseEntity.ok(ApiResponse.success("Data sync completed successfully"));
        } catch (Exception e) {
            log.error("Error during manual sync: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error during sync: " + e.getMessage()));
        }
    }

    @GetMapping("/districts/search")
    public ResponseEntity<ApiResponse<DistrictResponse>> searchDistrictByName(
            @RequestParam String name,
            @RequestParam(required = false) Long stateId) {
        log.info("Searching for district: {}, stateId: {}", name, stateId);
        try {
            DistrictResponse district = performanceService.searchDistrictByName(name, stateId);

            if (district == null) {
                return ResponseEntity.ok(ApiResponse.error("District not found: " + name));
            }

            return ResponseEntity.ok(ApiResponse.success(district));
        } catch (Exception e) {
            log.error("Error searching district: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error searching district: " + e.getMessage()));
        }
    }
}