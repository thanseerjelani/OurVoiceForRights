package com.mgnrega.dashboard.scheduler;

import com.mgnrega.dashboard.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheCleanupScheduler {

    private final GeocodingService geocodingService;

    // Cleanup cache every 2 hours
    @Scheduled(fixedRate = 7200000) // 2 hours in milliseconds
    public void cleanupGeocodingCache() {
        log.info("Running geocoding cache cleanup...");
        geocodingService.cleanupCache();
    }
}