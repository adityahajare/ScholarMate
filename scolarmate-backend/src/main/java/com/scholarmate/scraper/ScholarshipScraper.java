package com.scholarmate.scraper;

import com.scholarmate.entity.ScholarshipSource;

public interface ScholarshipScraper {
    String getScraperKey();
    String scrape(ScholarshipSource source);
}
