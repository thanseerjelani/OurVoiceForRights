package com.mgnrega.dashboard.service;

import com.mgnrega.dashboard.dto.MgnregaApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MgnregaApiService {

    private final WebClient webClient;

    @Value("${mgnrega.api.base-url}")
    private String baseUrl;

    @Value("${mgnrega.api.resource-id}")
    private String resourceId;

    @Value("${mgnrega.api.api-key}")
    private String apiKey;

    public MgnregaApiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.data.gov.in")
                .build();
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000),
            retryFor = {Exception.class}
    )
    public MgnregaApiResponse fetchMgnregaData(String state, String finYear) {
        log.info("Fetching MGNREGA data for state: {}, finYear: {}", state, finYear);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/resource/{resourceId}")
                            .queryParam("api-key", apiKey)
                            .queryParam("format", "json")
                            .queryParam("filters[state_name]", state)
                            .queryParam("filters[fin_year]", finYear)
                            .queryParam("limit", 1000)
                            .build(resourceId))
                    .retrieve()
                    .bodyToMono(MgnregaApiResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("Error fetching MGNREGA data: {}", e.getMessage());
            return createFallbackResponse();
        }
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public MgnregaApiResponse fetchAllData() {
        log.info("Fetching all MGNREGA data for Karnataka");

        try {
            // Fetch with increased limit and offset for pagination
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/resource/{resourceId}")
                            .queryParam("api-key", apiKey)
                            .queryParam("format", "json")
                            .queryParam("filters[state_name]", "KARNATAKA")
                            .queryParam("filters[fin_year]", "2024-2025")
                            .queryParam("limit", 100)  // Increased to 100
                            .queryParam("offset", 0)
                            .build(resourceId))
                    .retrieve()
                    .bodyToMono(MgnregaApiResponse.class)
                    .timeout(Duration.ofMinutes(2))
                    .block();
        } catch (Exception e) {
            log.error("Error fetching all MGNREGA data: {}", e.getMessage());
            return createFallbackResponse();
        }
    }

    // ✅ NEW: Fetch multiple years of data
    public List<MgnregaApiResponse> fetchMultipleYears(int yearsBack) {
        List<MgnregaApiResponse> allResponses = new ArrayList<>();

        for (int i = 0; i <= yearsBack; i++) {
            String finYear = getFinancialYear(i);
            log.info("Fetching data for financial year: {}", finYear);

            MgnregaApiResponse response = fetchDataForYear(finYear);
            if (response != null && response.getRecords() != null) {
                allResponses.add(response);
            }
        }

        return allResponses;
    }

    // ✅ NEW: Calculate financial year dynamically
    private String getFinancialYear(int yearsBack) {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int month = now.getMonthValue();

        // Financial year: April to March
        // If current month is April or later, FY is current-next year
        // If current month is Jan-March, FY is previous-current year
        int startYear = (month >= 4) ? currentYear : currentYear - 1;
        startYear -= yearsBack;

        return startYear + "-" + (startYear + 1);
    }

    // ✅ NEW: Fetch data for a specific year
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    private MgnregaApiResponse fetchDataForYear(String finYear) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/resource/{resourceId}")
                            .queryParam("api-key", apiKey)
                            .queryParam("format", "json")
                            .queryParam("filters[state_name]", "KARNATAKA")
                            .queryParam("filters[fin_year]", finYear)
                            .queryParam("limit", 100)
                            .queryParam("offset", 0)
                            .build(resourceId))
                    .retrieve()
                    .bodyToMono(MgnregaApiResponse.class)
                    .timeout(Duration.ofMinutes(2))
                    .block();
        } catch (Exception e) {
            log.error("Error fetching data for year {}: {}", finYear, e.getMessage());
            return createFallbackResponse();
        }
    }

    private MgnregaApiResponse createFallbackResponse() {
        MgnregaApiResponse response = new MgnregaApiResponse();
        response.setStatus("error");
        response.setTotal(0);
        return response;
    }
}