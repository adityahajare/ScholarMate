package com.scholarmate.scraper;

import com.scholarmate.entity.ScholarshipSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenericScrapingEngineTest {

    @Test
    void dailySourceIsDueAfterFiveHours() {
        ScholarshipSource source = new ScholarshipSource();
        source.setActive(true);
        source.setScrapingAllowed(true);
        source.setCrawlFrequency("DAILY");
        source.setLastSuccessfulScrapeAt(LocalDateTime.now().minusHours(6));

        assertTrue(GenericScrapingEngine.isSourceDueForScraping(source, LocalDateTime.now()));
    }

    @Test
    void weeklySourceIsNotDueBeforeFiveHours() {
        ScholarshipSource source = new ScholarshipSource();
        source.setActive(true);
        source.setScrapingAllowed(true);
        source.setCrawlFrequency("WEEKLY");
        source.setLastSuccessfulScrapeAt(LocalDateTime.now().minusHours(4));

        assertFalse(GenericScrapingEngine.isSourceDueForScraping(source, LocalDateTime.now()));
    }

    @Test
    void monthlySourceRunsWhenNoPreviousSuccessWasRecorded() {
        ScholarshipSource source = new ScholarshipSource();
        source.setActive(true);
        source.setScrapingAllowed(true);
        source.setCrawlFrequency("MONTHLY");

        assertTrue(GenericScrapingEngine.isSourceDueForScraping(source, LocalDateTime.now()));
    }
}
