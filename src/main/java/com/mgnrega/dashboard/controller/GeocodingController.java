package com.mgnrega.dashboard.controller;

import com.mgnrega.dashboard.dto.ApiResponse;
import com.mgnrega.dashboard.dto.GeocodingResponse;
import com.mgnrega.dashboard.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "https://voicesforrights.netlify.app")
public class GeocodingController {

    private final GeocodingService geocodingService;

    /**
     * Reverse geocode coordinates to get district and state information
     * Uses OpenStreetMap Nominatim API
     *
     * @param lat Latitude (-90 to 90)
     * @param lon Longitude (-180 to 180)
     * @return District and state information
     */
    @GetMapping("/geocode")
    public ResponseEntity<ApiResponse<GeocodingResponse>> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lon) {

        log.info("Geocoding request received for lat: {}, lon: {}", lat, lon);

        try {
            // Validate coordinates
            if (lat < -90 || lat > 90) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid latitude. Must be between -90 and 90"));
            }

            if (lon < -180 || lon > 180) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid longitude. Must be between -180 and 180"));
            }

            GeocodingResponse response = geocodingService.reverseGeocode(lat, lon);

            // Check if we got valid district information
            if (response.getDistrict() == null || response.getDistrict().isEmpty()) {
                log.warn("No district information found for coordinates: {}, {}", lat, lon);
                return ResponseEntity.ok(
                        ApiResponse.error("Could not determine district for the given location")
                );
            }

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Geocoding error: {}", e.getMessage());

            // Check if it's a rate limit or service unavailable error
            if (e.getMessage().contains("Rate limit") || e.getMessage().contains("try again")) {
                return ResponseEntity.status(429) // Too Many Requests
                        .body(ApiResponse.error("Service temporarily unavailable. Please try again in a moment."));
            }

            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Data not available. Please try again later."));
        } catch (Exception e) {
            log.error("Unexpected error during geocoding: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Data not available. Please try again later."));
        }
    }

    /**
     * Get geocoding service statistics
     */
    @GetMapping("/geocode/stats")
    public ResponseEntity<ApiResponse<String>> getStats() {
        int cacheSize = geocodingService.getCacheSize();
        String stats = String.format("Geocoding cache size: %d entries", cacheSize);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}