package com.mgnrega.dashboard.service;

import com.mgnrega.dashboard.dto.GeocodingResponse;
import com.mgnrega.dashboard.dto.NominatimResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class GeocodingService {

    private final WebClient webClient;

    @Value("${app.name:MGNREGA-Dashboard}")
    private String appName;

    @Value("${app.contact.email:support@mgnrega-dashboard.com}")
    private String contactEmail;

    // Rate limiting: Nominatim allows 1 request per second
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 1000; // 1 second

    // Simple in-memory cache for recent geocoding requests
    private final ConcurrentHashMap<String, CachedLocation> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 3600000; // 1 hour

    public GeocodingService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://nominatim.openstreetmap.org")
                .build();
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000),
            retryFor = {Exception.class}
    )
    public GeocodingResponse reverseGeocode(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        // Check cache first
        String cacheKey = getCacheKey(lat, lon);
        CachedLocation cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.info("Returning cached geocoding result for: {}", cacheKey);
            return cached.getResponse();
        }

        // Rate limiting - ensure we don't exceed 1 request per second
        enforceRateLimit();

        log.info("Fetching geocoding data from OpenStreetMap Nominatim for lat: {}, lon: {}", lat, lon);

        try {
            NominatimResponse nominatimResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("format", "json")
                            .queryParam("addressdetails", 1)
                            .build())
                    .header("User-Agent", appName + "/1.0 (" + contactEmail + ")")
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(NominatimResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (nominatimResponse == null || nominatimResponse.getAddress() == null) {
                throw new RuntimeException("Invalid response from Nominatim API");
            }

            GeocodingResponse response = buildGeocodingResponse(nominatimResponse, lat, lon);

            // Cache the result
            cache.put(cacheKey, new CachedLocation(response, System.currentTimeMillis()));

            log.info("Successfully geocoded location: {}, {}", response.getDistrict(), response.getState());
            return response;

        } catch (WebClientResponseException.TooManyRequests e) {
            log.error("Rate limit exceeded for Nominatim API");
            throw new RuntimeException("Rate limit exceeded. Please try again later.");
        } catch (WebClientResponseException.Forbidden e) {
            log.error("Forbidden: Check User-Agent header configuration");
            throw new RuntimeException("Access forbidden. Please check API configuration.");
        } catch (WebClientResponseException e) {
            log.error("HTTP error from Nominatim: {} - {}", e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Failed to fetch location details. Please try again later.");
        } catch (Exception e) {
            log.error("Error fetching geocoding data: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch location details: " + e.getMessage());
        }
    }

    private void enforceRateLimit() {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;

            if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
                long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
                log.debug("Rate limiting: sleeping for {} ms", sleepTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Rate limit sleep interrupted");
                }
            }

            lastRequestTime = System.currentTimeMillis();
        }
    }

    private GeocodingResponse buildGeocodingResponse(NominatimResponse nominatimResponse, Double lat, Double lon) {
        GeocodingResponse.Address address = nominatimResponse.getAddress();

        // Extract district - try multiple fields in order of preference
        String district = null;

        if (address.getCounty() != null && !address.getCounty().isEmpty()) {
            district = address.getCounty();
        } else if (address.getStateDistrict() != null && !address.getStateDistrict().isEmpty()) {
            district = address.getStateDistrict();
        } else if (address.getCity() != null && !address.getCity().isEmpty()) {
            district = address.getCity();
        } else if (address.getTown() != null && !address.getTown().isEmpty()) {
            district = address.getTown();
        } else if (address.getMunicipality() != null && !address.getMunicipality().isEmpty()) {
            district = address.getMunicipality();
        } else if (address.getVillage() != null && !address.getVillage().isEmpty()) {
            district = address.getVillage();
        }

        // Clean up district name if it contains "District" suffix
        if (district != null && district.toLowerCase().endsWith(" district")) {
            district = district.substring(0, district.length() - 9).trim();
        }

        return GeocodingResponse.builder()
                .district(district)
                .state(address.getState())
                .lat(lat)
                .lon(lon)
                .address(address)
                .build();
    }

    private String getCacheKey(Double lat, Double lon) {
        // Round to 3 decimal places (~100m precision) for cache key
        return String.format("%.3f,%.3f", lat, lon);
    }

    // Inner class for caching
    private static class CachedLocation {
        private final GeocodingResponse response;
        private final long timestamp;

        public CachedLocation(GeocodingResponse response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }

        public GeocodingResponse getResponse() {
            return response;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    // Cleanup expired cache entries
    public void cleanupCache() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.info("Cleaned up geocoding cache. Current size: {}", cache.size());
    }

    // Get cache statistics
    public int getCacheSize() {
        return cache.size();
    }
}