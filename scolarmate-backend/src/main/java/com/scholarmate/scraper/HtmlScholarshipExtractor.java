package com.scholarmate.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class HtmlScholarshipExtractor {

    public String extractText(String url) {

        try {

            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(20000)
                    .get();

            return document.body().text();

        } catch (Exception e) {

            return "HTML extraction failed: " + e.getMessage();
        }
    }
}