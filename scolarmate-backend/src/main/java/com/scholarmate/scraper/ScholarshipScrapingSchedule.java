package com.scholarmate.scraper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "scholarmate.scraper.schedule.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ScholarshipScrapingSchedule {

    private final GenericScrapingEngine scrapingEngine;

    public ScholarshipScrapingSchedule(GenericScrapingEngine scrapingEngine) {
        this.scrapingEngine = scrapingEngine;
    }

    @Scheduled(
            initialDelayString = "${scholarmate.scraper.initial-delay-ms:0}",
            fixedDelayString = "${scholarmate.scraper.poll-interval-ms:18000000}")
    public void scrapeOnSchedule() {
        scrapingEngine.scrapeAllSourcesOnSchedule();
    }
}
