package com.scholarmate.scraper;

import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class GenericScrapingEngine {

    private static final Logger log = LoggerFactory.getLogger(GenericScrapingEngine.class);

    private final ScholarshipSourceRepository sourceRepository;
    private final ScholarshipScraperRegistry scraperRegistry;
    @Value("${scholarmate.scraper.max-concurrent-sources:6}")
    private int configuredConcurrency;
    private final ExecutorService executor;

    @Autowired
    public GenericScrapingEngine(ScholarshipSourceRepository sourceRepository,
                                 ScholarshipScraperRegistry scraperRegistry,
                                 ExecutorService executor) {
        this.sourceRepository = sourceRepository;
        this.scraperRegistry = scraperRegistry;
        this.executor = executor;
    }

    public String scrapeAllActiveSources() {
        List<ScholarshipSource> sources = sourceRepository.findAll()
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()) && Boolean.TRUE.equals(s.getScrapingAllowed()))
                .toList();

        if (sources.isEmpty()) {
            return "No active scholarship sources found.";
        }
        return scrapeSources(sources);
    }

    public void scrapeAllSourcesOnSchedule() {
        LocalDateTime now = LocalDateTime.now();
        List<ScholarshipSource> activeSources = sourceRepository.findAll()
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .filter(s -> Boolean.TRUE.equals(s.getScrapingAllowed()))
                .filter(s -> isSourceDueForScraping(s, now))
                .toList();
        scrapeSources(activeSources);
    }

    private String scrapeSources(List<ScholarshipSource> sources) {
        if (sources.isEmpty()) {
            return "No scholarship sources are due for scraping.";
        }
        int concurrency = Math.min(Math.max(1, configuredConcurrency), sources.size());
        // executor is injected and managed by Spring; we rely on it directly.
        List<Future<String>> futures = new ArrayList<>(sources.size());
        long startedAt = System.nanoTime();

        try {
            for (ScholarshipSource source : sources) {
                futures.add(executor.submit(() -> scrapeSource(source)));
            }
            StringBuilder result = new StringBuilder();
            for (Future<String> future : futures) {
                try {
                    result.append(future.get()).append("\n");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    result.append("Scrape task interrupted.\n");
                    break;
                } catch (ExecutionException e) {
                    result.append("Scrape task failed: ")
                          .append(e.getCause() == null ? e.getMessage() : e.getCause().getMessage())
                          .append("\n");
                }
            }
            log.info("Scrape batch completed sources={} concurrency={} durationMs={}",
                    sources.size(), concurrency, elapsedMillis(startedAt));
            return result.toString();
        } finally {
            // Do not shut down the shared executor here; Spring manages its lifecycle.
        }
    }

    private String scrapeSource(ScholarshipSource source) {
        LocalDateTime attemptedAt = LocalDateTime.now();
        long startedAt = System.nanoTime();
        try {
            ScholarshipScraper scraper = scraperRegistry.getScraper(source);
            String response = scraper.scrape(source);
            source.setLastScrapedAt(attemptedAt);
            if (!response.toLowerCase().contains("failed") && !response.startsWith("No valid source URL")) {
                source.setLastSuccessfulScrapeAt(attemptedAt);
            }
            sourceRepository.save(source);
            log.info("Scrape source={} status=success durationMs={}", source.getName(), elapsedMillis(startedAt));
            return source.getName() + " -> " + response;
        } catch (Exception e) {
            source.setLastScrapedAt(attemptedAt);
            sourceRepository.save(source);
            log.warn("Scrape source={} status=failed durationMs={} error={}", source.getName(), elapsedMillis(startedAt), e.getMessage());
            return source.getName() + " -> FAILED: " + e.getMessage();
        }
    }

    static boolean isSourceDueForScraping(ScholarshipSource source, LocalDateTime now) {
        if (source == null || !Boolean.TRUE.equals(source.getActive()) || !Boolean.TRUE.equals(source.getScrapingAllowed())) {
            return false;
        }
        if (source.getCrawlFrequency() == null || source.getCrawlFrequency().isBlank()) {
            return true;
        }
        LocalDateTime lastSuccessfulAttempt = source.getLastSuccessfulScrapeAt() != null
                ? source.getLastSuccessfulScrapeAt()
                : source.getLastScrapedAt();
        if (lastSuccessfulAttempt == null) {
            return true;
        }
        // Assuming crawlFrequency is a number of hours stored as a string; fallback to 5 hours if parsing fails.
        int hours;
        try {
            hours = Integer.parseInt(source.getCrawlFrequency());
        } catch (Exception ex) {
            hours = 5;
        }
        return !now.isBefore(lastSuccessfulAttempt.plusHours(hours));
    }

    private long elapsedMillis(long startNano) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
    }
}
