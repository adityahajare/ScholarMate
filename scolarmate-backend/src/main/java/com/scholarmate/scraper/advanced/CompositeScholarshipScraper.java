package com.scholarmate.scraper.advanced;

import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.scraper.ScholarshipScraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Composite scraper that can handle multiple resource types (HTML, PDF, JSON API, etc.).
 * It delegates the actual extraction to a collection of specialized scrapers and aggregates
 * their results into a single response string.
 */
@Component
public class CompositeScholarshipScraper implements ScholarshipScraper {

    private static final Logger log = LoggerFactory.getLogger(CompositeScholarshipScraper.class);

    private final List<ScholarshipScraper> scrapers;

    @Autowired
    public CompositeScholarshipScraper(List<ScholarshipScraper> scrapers) {
        // The list will contain all concrete scraper beans (including HTML, PDF, etc.)
        this.scrapers = scrapers;
    }

    @Override
    public String getScraperKey() {
        // Use a generic key that matches sources configured for "composite"
        return "composite";
    }

    @Override
    public String scrape(ScholarshipSource source) {
        StringBuilder aggregate = new StringBuilder();
        log.info("Composite scraper starting for source {}", source.getName());
        for (ScholarshipScraper scraper : scrapers) {
            // Skip self to avoid infinite recursion
            if (scraper == this) {
                continue;
            }
            // Only invoke scrapers that claim to support this source (by matching a skill flag if present)
            // For simplicity, we call all and let each implementation decide if it can handle the URL.
            try {
                String result = scraper.scrape(source);
                if (result != null && !result.isBlank()) {
                    aggregate.append(result).append("\n");
                }
            } catch (Exception e) {
                log.warn("Scraper {} failed for source {}: {}", scraper.getClass().getSimpleName(), source.getName(), e.getMessage());
            }
        }
        if (aggregate.length() == 0) {
            return "No data extracted by any scraper for source " + source.getName();
        }
        return aggregate.toString().trim();
    }
}
