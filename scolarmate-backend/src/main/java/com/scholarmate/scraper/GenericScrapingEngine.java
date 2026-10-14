package com.scholarmate.scraper;

import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class GenericScrapingEngine {

    private static final Logger log = LoggerFactory.getLogger(GenericScrapingEngine.class);

    private final ScholarshipSourceRepository sourceRepository;
    private final ScholarshipScraperRegistry scraperRegistry;
    @Value("${scholarmate.scraper.max-concurrent-sources:6}")
    private int configuredConcurrency;

    public GenericScrapingEngine(
            ScholarshipSourceRepository sourceRepository,
            ScholarshipScraperRegistry scraperRegistry) {

        this.sourceRepository = sourceRepository;
        this.scraperRegistry = scraperRegistry;
    }

    public String scrapeAllActiveSources() {

        List<ScholarshipSource> sources =
                sourceRepository.findAll()
                        .stream()
                        .filter(source ->
                    Boolean.TRUE.equals(source.getActive())
                        && Boolean.TRUE.equals(source.getScrapingAllowed()))
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
                .filter(source -> Boolean.TRUE.equals(source.getActive()))
                .filter(source -> Boolean.TRUE.equals(source.getScrapingAllowed()))
                .filter(source -> isSourceDueForScraping(source, now))
                .toList();

        scrapeSources(activeSources);
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

        return !now.isBefore(lastSuccessfulAttempt.plus(Duration.ofHours(5)));
    }

    private String scrapeSources(List<ScholarshipSource> sources) {
        if (sources.isEmpty()) {
            return "No scholarship sources are due for scraping.";
        }

        int concurrency = Math.min(Math.max(1, configuredConcurrency), sources.size());
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
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
            executor.shutdown();
        }
    }

    private String scrapeSource(ScholarshipSource source) {
        LocalDateTime attemptedAt = LocalDateTime.now();
        long startedAt = System.nanoTime();

        try {
            ScholarshipScraper scraper = scraperRegistry.getScraper(source);
            String response = scraper.scrape(source);
            source.setLastScrapedAt(attemptedAt);
            if (!response.toLowerCase().contains("failed")
                    && !response.startsWith("No valid source URL")) {
                source.setLastSuccessfulScrapeAt(attemptedAt);
            }
            sourceRepository.save(source);
            log.info("Scrape source={} status=success durationMs={}", source.getName(), elapsedMillis(startedAt));
            return source.getName() + " -> " + response;
        } catch (Exception e) {
            source.setLastScrapedAt(attemptedAt);
            sourceRepository.save(source);
            log.warn("Scrape source={} status=failed durationMs={} error={}",
                    source.getName(), elapsedMillis(startedAt), e.getMessage());
            return source.getName() + " -> FAILED: " + e.getMessage();
        }
    }

    private static long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }
}
