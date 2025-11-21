package com.mgnrega.dashboard.service;

import com.mgnrega.dashboard.dto.*;
import com.mgnrega.dashboard.entity.District;
import com.mgnrega.dashboard.entity.Performance;
import com.mgnrega.dashboard.entity.State;
import com.mgnrega.dashboard.repository.DistrictRepository;
import com.mgnrega.dashboard.repository.PerformanceRepository;
import com.mgnrega.dashboard.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final PerformanceRepository performanceRepository;
    private final MgnregaApiService mgnregaApiService;

    @Cacheable("states")
    public List<StateResponse> getAllStates() {
        return stateRepository.findAll().stream()
                .map(this::mapToStateResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "districts", key = "#stateId")
    public List<DistrictResponse> getDistrictsByState(Long stateId) {
        return districtRepository.findByStateId(stateId).stream()
                .map(this::mapToDistrictResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "performance", key = "#districtId")
    public PerformanceResponse getLatestPerformance(Long districtId) {
        Performance performance = performanceRepository
                .findTopByDistrictIdOrderByFinYearDescMonthNameDesc(districtId)
                .orElse(null);

        if (performance == null) {
            return null;
        }

        return mapToPerformanceResponse(performance);
    }

    public ComparisonResponse getComparison(Long districtId, String year) {
        List<Performance> performances = performanceRepository
                .findByDistrictIdOrderByYearDescMonthDesc(districtId, PageRequest.of(0, 2))
                .getContent();

        if (performances.isEmpty()) {
            return null;
        }

        Performance current = performances.get(0);
        Performance previous = performances.size() > 1 ? performances.get(1) : null;

        ComparisonMetrics metrics = null;
        if (previous != null) {
            metrics = ComparisonMetrics.builder()
                    .householdsChange(calculatePercentageChange(
                            previous.getTotalHouseholdsWorked(),
                            current.getTotalHouseholdsWorked()))
                    .daysWorkedChange(calculatePercentageChange(
                            previous.getAverageDaysEmployment(),
                            current.getAverageDaysEmployment()))
                    .wagesChange(calculatePercentageChange(
                            previous.getTotalWages(),
                            current.getTotalWages()))
                    .projectsChange((int)(current.getOngoingWorks() - previous.getOngoingWorks()))
                    .build();
        }

        return ComparisonResponse.builder()
                .current(mapToPerformanceResponse(current))
                .previous(previous != null ? mapToPerformanceResponse(previous) : null)
                .comparison(metrics)
                .build();
    }

    @Transactional
    public void syncDataFromApi() {
        log.info("Starting MGNREGA data sync...");

        try {
            MgnregaApiResponse apiResponse = mgnregaApiService.fetchAllData();

            if (apiResponse == null || apiResponse.getRecords() == null) {
                log.warn("No data received from MGNREGA API");
                return;
            }

            log.info("Received {} records from API", apiResponse.getRecords().size());

            int successCount = 0;
            int errorCount = 0;

            for (MgnregaRecord record : apiResponse.getRecords()) {
                try {
                    savePerformanceRecord(record);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error saving record for district: {} - {}",
                            record.getDistrict_name(), e.getMessage());
                }
            }

            log.info("Data sync completed: {} successful, {} errors", successCount, errorCount);
        } catch (Exception e) {
            log.error("Error during data sync: {}", e.getMessage(), e);
        }
    }

    private void savePerformanceRecord(MgnregaRecord record) {
        State state = stateRepository.findByName(record.getState_name())
                .orElseGet(() -> {
                    State newState = new State();
                    newState.setName(record.getState_name());
                    newState.setStateCode(record.getState_code());
                    return stateRepository.save(newState);
                });

        District district = districtRepository
                .findByNameAndStateId(record.getDistrict_name(), state.getId())
                .orElseGet(() -> {
                    District newDistrict = new District();
                    newDistrict.setName(record.getDistrict_name());
                    newDistrict.setDistrictCode(record.getDistrict_code());
                    newDistrict.setState(state);
                    return districtRepository.save(newDistrict);
                });

        Performance performance = new Performance();
        performance.setDistrict(district);
        performance.setMonthName(record.getMonth());
        performance.setFinYear(record.getFin_year());

        // These now work correctly with Object type
        performance.setTotalHouseholdsWorked(parseLong(record.getTotal_Households_Worked()));
        performance.setAverageDaysEmployment(parseBigDecimal(record.getAverage_days_of_employment_provided_per_Household()));
        performance.setTotalWages(parseBigDecimal(record.getWages()));
        performance.setOngoingWorks(parseLong(record.getNumber_of_Ongoing_Works()));
        performance.setCompletedWorks(parseLong(record.getNumber_of_Completed_Works()));
        performance.setTotalExpenditure(parseBigDecimal(record.getTotal_Exp()));
        performance.setAvgWageRate(parseBigDecimal(record.getAverage_Wage_rate_per_day_per_person()));
        performance.setTimestamp(LocalDateTime.now());
        performance.setDataSource("data.gov.in");

        performanceRepository.save(performance);
        log.info("Saved performance record for district: {} - {}/{}",
                district.getName(), record.getMonth(), record.getFin_year());
    }

    private StateResponse mapToStateResponse(State state) {
        return StateResponse.builder()
                .id(state.getId())
                .name(state.getName())
                .stateCode(state.getStateCode())
                .build();
    }

    private DistrictResponse mapToDistrictResponse(District district) {
        return DistrictResponse.builder()
                .id(district.getId())
                .name(district.getName())
                .districtCode(district.getDistrictCode())
                .stateId(district.getState().getId())
                .stateName(district.getState().getName())
                .build();
    }

    private PerformanceResponse mapToPerformanceResponse(Performance p) {
        return PerformanceResponse.builder()
                .id(p.getId())
                .districtId(p.getDistrict().getId())
                .districtName(p.getDistrict().getName())
                .monthName(p.getMonthName())
                .finYear(p.getFinYear())
                .totalHouseholdsWorked(p.getTotalHouseholdsWorked())
                .averageDaysEmployment(p.getAverageDaysEmployment())
                .totalWages(p.getTotalWages())
                .ongoingWorks(p.getOngoingWorks())
                .completedWorks(p.getCompletedWorks())
                .totalExpenditure(p.getTotalExpenditure())
                .avgWageRate(p.getAvgWageRate())
                .lastUpdated(p.getTimestamp())
                .performanceLevel(calculatePerformanceLevel(p))
                .build();
    }

    /**
     * Calculate performance level based on average days of employment.
     *
     * MGNREGA Context:
     * - Legal guarantee: 100 days per household per year
     * - National average: ~45-50 days
     * - Good performance: 50+ days
     * - Excellent performance: 60+ days
     *
     * Thresholds:
     * - ABOVE_AVERAGE: >= 50 days (good achievement)
     * - MODERATE: 35-49 days (acceptable, near national average)
     * - BELOW_AVERAGE: < 35 days (needs improvement)
     */
    private String calculatePerformanceLevel(Performance p) {
        if (p.getAverageDaysEmployment() != null) {
            double days = p.getAverageDaysEmployment().doubleValue();

            // ABOVE_AVERAGE: 50+ days (50% of legal guarantee)
            if (days >= 50) return "ABOVE_AVERAGE";

            // MODERATE: 35-49 days (acceptable performance)
            if (days >= 35) return "MODERATE";

            // BELOW_AVERAGE: < 35 days (needs improvement)
            return "BELOW_AVERAGE";
        }

        // Default to MODERATE if data unavailable
        return "MODERATE";
    }

    private BigDecimal calculatePercentageChange(Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null) return BigDecimal.ZERO;

        BigDecimal old = convertToBigDecimal(oldValue);
        BigDecimal newVal = convertToBigDecimal(newValue);

        if (old.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return newVal.subtract(old)
                .divide(old, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Long) return BigDecimal.valueOf((Long) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        return BigDecimal.ZERO;
    }

    private Long parseLong(Object value) {
        try {
            if (value == null) return 0L;

            // Handle if it's already a Number
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }

            // Handle if it's a String
            String strValue = value.toString().replaceAll("[^0-9]", "");
            return strValue.isEmpty() ? 0L : Long.parseLong(strValue);

        } catch (Exception e) {
            log.warn("Error parsing long value: {} - {}", value, e.getMessage());
            return 0L;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        try {
            if (value == null) return BigDecimal.ZERO;

            // Handle if it's already a Number
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }

            // Handle if it's a String
            String strValue = value.toString().replaceAll("[^0-9.]", "");

            // Handle edge case of multiple dots
            int firstDot = strValue.indexOf('.');
            if (firstDot != -1) {
                strValue = strValue.substring(0, firstDot + 1) +
                        strValue.substring(firstDot + 1).replace(".", "");
            }

            return strValue.isEmpty() ? BigDecimal.ZERO : new BigDecimal(strValue);

        } catch (Exception e) {
            log.warn("Error parsing BigDecimal value: {} - {}", value, e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    public DistrictResponse searchDistrictByName(String name, Long stateId) {
        log.info("Searching for district with name: {}", name);

        // Try exact match first
        Optional<District> districtOpt;

        if (stateId != null) {
            districtOpt = districtRepository.findByNameAndStateId(name, stateId);
        } else {
            // Search across all states
            districtOpt = districtRepository.findAll().stream()
                    .filter(d -> normalizeDistrictName(d.getName()).equals(normalizeDistrictName(name)))
                    .findFirst();
        }

        if (districtOpt.isPresent()) {
            return mapToDistrictResponse(districtOpt.get());
        }

        // Try fuzzy matching if exact match fails
        List<District> allDistricts = stateId != null
                ? districtRepository.findByStateId(stateId)
                : districtRepository.findAll();

        for (District district : allDistricts) {
            if (isSimilarDistrictName(district.getName(), name)) {
                log.info("Found similar district: {} for search term: {}", district.getName(), name);
                return mapToDistrictResponse(district);
            }
        }

        log.warn("No district found for: {}", name);
        return null;
    }

    // Helper method to normalize ALL Karnataka district names
    private String normalizeDistrictName(String name) {
        if (name == null) return "";

        return name.toUpperCase()
                .replace(" DISTRICT", "")
                .replace("DISTRICT", "")
                // Official name changes (old → new)
                .replace("BENGALURU", "BANGALORE")
                .replace("BANGALORE", "BENGALURU") // Handle both ways
                .replace("MYSURU", "MYSORE")
                .replace("MYSORE", "MYSURU")
                .replace("BELAGAVI", "BELGAUM")
                .replace("BELGAUM", "BELAGAVI")
                .replace("HUBBALLI", "HUBLI")
                .replace("HUBLI", "HUBBALLI")
                .replace("SHIVAMOGGA", "SHIMOGA")
                .replace("SHIMOGA", "SHIVAMOGGA")
                .replace("TUMAKURU", "TUMKUR")
                .replace("TUMKUR", "TUMAKURU")
                .replace("BALLARI", "BELLARY")
                .replace("BELLARY", "BALLARI")
                .replace("VIJAYAPURA", "BIJAPUR")
                .replace("BIJAPUR", "VIJAYAPURA")
                .replace("KALABURAGI", "GULBARGA")
                .replace("GULBARGA", "KALABURAGI")
                .replace("CHIKKAMAGALURU", "CHIKMAGALUR")
                .replace("CHIKMAGALUR", "CHIKKAMAGALURU")
                .replace("CHAMARAJANAGARA", "CHAMARAJANAGAR")
                .replace("CHAMARAJANAGAR", "CHAMARAJANAGARA")
                .replace("VIJAYAPURA", "BIJAPUR")
                .replace("YADGIR", "YADAGIRI")
                .replace("YADAGIRI", "YADGIR")
                // Variations
                .replace("DAKSHINA KANNADA", "SOUTH CANARA")
                .replace("SOUTH CANARA", "DAKSHINA KANNADA")
                .replace("D.K.", "DAKSHINA KANNADA")
                .replace("DK", "DAKSHINA KANNADA")
                .replace("UTTARA KANNADA", "NORTH CANARA")
                .replace("NORTH CANARA", "UTTARA KANNADA")
                .replace("U.K.", "UTTARA KANNADA")
                .replace("UK", "UTTARA KANNADA")
                .replace("BANGALORE URBAN", "BENGALURU URBAN")
                .replace("BANGALORE RURAL", "BENGALURU RURAL")
                .replace("MYSORE CITY", "MYSURU")
                // Handle "Urban" and "Rural" suffixes
                .replace(" URBAN", "")
                .replace(" RURAL", "")
                .replace("URBAN", "")
                .replace("RURAL", "")
                .trim();
    }

    // Helper method to check if district names are similar
    private boolean isSimilarDistrictName(String dbName, String searchName) {
        String normalizedDb = normalizeDistrictName(dbName);
        String normalizedSearch = normalizeDistrictName(searchName);

        // Exact match after normalization
        if (normalizedDb.equals(normalizedSearch)) {
            return true;
        }

        // Check if one contains the other
        if (normalizedDb.contains(normalizedSearch) || normalizedSearch.contains(normalizedDb)) {
            return true;
        }

        // Check without spaces (e.g., "Chickmagalur" vs "Chikmagalur")
        String dbNoSpace = normalizedDb.replace(" ", "");
        String searchNoSpace = normalizedSearch.replace(" ", "");
        if (dbNoSpace.contains(searchNoSpace) || searchNoSpace.contains(dbNoSpace)) {
            return true;
        }

        return false;
    }
}