package com.scholarmate.scraper;

import com.scholarmate.entity.ScholarshipSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScholarshipScraperRegistry {

    private final List<ScholarshipScraper> scrapers;

    public ScholarshipScraperRegistry(
            List<ScholarshipScraper> scrapers) {
        this.scrapers = scrapers;
    }

    public ScholarshipScraper getScraper(
            ScholarshipSource source) {

        return scrapers.stream()
                .filter(scraper ->
                        scraper.getScraperKey()
                                .equalsIgnoreCase(
                                        source.getScraperKey()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No scraper found for source: "
                                        + source.getName()));
    }
}