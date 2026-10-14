package com.scholarmate.controller;

import com.scholarmate.scraper.GenericScrapingEngine;
import com.scholarmate.scraper.NspScholarshipScraper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

    private final NspScholarshipScraper nspScholarshipScraper;
    private final GenericScrapingEngine genericScrapingEngine;

    public ScraperController(
            NspScholarshipScraper nspScholarshipScraper,
            GenericScrapingEngine genericScrapingEngine) {
        this.nspScholarshipScraper = nspScholarshipScraper;
        this.genericScrapingEngine = genericScrapingEngine;
    }

    @PostMapping("/nsp/run")
    public String runNsp() {
        return nspScholarshipScraper.scrapeAndSave();
    }

    @PostMapping("/run-all")
    public String runAllSources() {
        return genericScrapingEngine.scrapeAllActiveSources();
    }
}