package com.scholarmate.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * Simple HTML scraper using Jsoup. Fetches the entire body text of a page.
 * In a real app you would add timeout handling, user‑agent control, and
 * domain whitelist validation. For now we keep it minimal.
 */
@Component
public class HtmlScraper {

    /**
     * Retrieves the main textual content of the given URL.
     *
     * @param url absolute URL (http or https)
     * @return plain text extracted from the HTML body
     */
    public String fetchContent(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("ScholarmateBot/1.0")
                    .timeout(10_000)
                    .get();
            return doc.body().text();
        } catch (IOException e) {
            // Propagate as a RuntimeException so GlobalExceptionHandler can turn it into 500
            throw new RuntimeException("Failed to scrape URL: " + e.getMessage(), e);
        }
    }
}

