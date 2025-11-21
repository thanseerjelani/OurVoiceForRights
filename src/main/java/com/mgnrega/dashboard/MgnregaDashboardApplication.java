package com.mgnrega.dashboard;

import com.mgnrega.dashboard.repository.StateRepository;
import com.mgnrega.dashboard.service.PerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableRetry
@Slf4j
public class MgnregaDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(MgnregaDashboardApplication.class, args);
	}

	/**
	 * Initialize data on application startup if database is empty.
	 * This ensures data is available immediately after deployment.
	 */
	@Bean
	public CommandLineRunner initializeData(
			PerformanceService performanceService,
			StateRepository stateRepository) {

		return args -> {
			log.info("🚀 ========================================");
			log.info("🚀 Checking initial data sync requirement...");
			log.info("🚀 ========================================");

			try {
				// Check if states table has data
				long stateCount = stateRepository.count();
				log.info("📊 Current states in database: {}", stateCount);

				if (stateCount == 0) {
					log.info("⚠️  Database is empty. Starting initial data sync...");
					log.info("⏳ This may take 30-60 seconds. Please wait...");

					long startTime = System.currentTimeMillis();
					performanceService.syncDataFromApi();
					long duration = (System.currentTimeMillis() - startTime) / 1000;

					log.info("✅ ========================================");
					log.info("✅ Initial data sync completed successfully!");
					log.info("✅ Duration: {} seconds", duration);
					log.info("✅ ========================================");

				} else {
					log.info("✅ Data already exists in database (States: {})", stateCount);
					log.info("✅ Skipping initial sync. Scheduled sync will run at 2 AM daily.");
					log.info("✅ Use POST /api/sync for manual refresh if needed.");
				}

			} catch (Exception e) {
				log.error("❌ ========================================");
				log.error("❌ Error during initial data sync: {}", e.getMessage(), e);
				log.error("❌ Application will continue but data may be unavailable.");
				log.error("❌ Please trigger manual sync: POST /api/sync");
				log.error("❌ ========================================");
				// Don't stop application startup even if sync fails
				// This allows manual sync via API endpoint
			}
		};
	}
}