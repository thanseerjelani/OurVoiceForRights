package com.mgnrega.dashboard.scheduler;

import com.mgnrega.dashboard.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSyncScheduler {

    private final PerformanceService performanceService;

    // Runs every day at 2 AM
    @Scheduled(cron = "${scheduler.data-refresh.cron}")
    public void syncMgnregaData() {
        log.info("Starting scheduled MGNREGA data sync...");
        try {
            performanceService.syncDataFromApi();
            log.info("Scheduled data sync completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled data sync: {}", e.getMessage(), e);
        }
    }

    // Manual trigger every 5 minutes for testing (remove in production)
    // @Scheduled(fixedRate = 300000)
    // public void testSync() {
    //     log.info("Test sync triggered");
    //     syncMgnregaData();
    // }
}