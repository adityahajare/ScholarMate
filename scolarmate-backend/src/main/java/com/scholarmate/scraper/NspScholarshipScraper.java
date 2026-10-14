package com.scholarmate.scraper;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.ScholarshipSourceRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NspScholarshipScraper implements ScholarshipScraper {

    private static final List<String> CANDIDATE_PATHS = List.of(
            "/All-Scholarships",
            "/scholarship-schemes",
            "/scholarships",
            "/all-scholarships"
    );

    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipSourceRepository sourceRepository;
    private final PdfScholarshipExtractor pdfScholarshipExtractor;

    public NspScholarshipScraper(
            ScholarshipRepository scholarshipRepository,
            ScholarshipSourceRepository sourceRepository,
            PdfScholarshipExtractor pdfScholarshipExtractor) {
        this.scholarshipRepository = scholarshipRepository;
        this.sourceRepository = sourceRepository;
        this.pdfScholarshipExtractor = pdfScholarshipExtractor;
    }

    public String scrapeAndSave() {
        ScholarshipSource source = sourceRepository.findByName("National Scholarship Portal (NSP)")
            .or(() -> sourceRepository.findByName("National Scholarship Portal"))
                .orElseThrow(() -> new RuntimeException("NSP source not registered"));
        return scrape(source);
    }

    @Override
    public String getScraperKey() {
        return "NSP";
    }

    @Override
    public String scrape(ScholarshipSource source) {
        if (source == null) {
            return "No scholarship source provided.";
        }

        String listingUrl = resolveUsableListingUrl(source.getBaseUrl());
        if (listingUrl == null) {
            return "No valid scholarship listing page was found for source: " + source.getName();
        }

        try {
            Document document = fetchDocument(listingUrl);
            Map<String, Scholarship> scholarships = new LinkedHashMap<>();

            for (Element anchor : document.select("a[href]")) {
                String rawText = sanitizeTitle(anchor.text());
                String href = anchor.absUrl("href");
                if (rawText.isBlank() || href.isBlank() || !isLikelyScholarshipLink(rawText, href)) {
                    continue;
                }

                String title = rawText;
                Element container = anchor.closest("li, tr, div, article, section, td");
                String details = container != null ? container.text() : anchor.text();
                String applicationUrl = null;
                String specificationUrl = null;
                String faqUrl = null;

                for (Element link : (container != null ? container.select("a[href]") : document.select("a[href]"))) {
                    String linkText = sanitizeTitle(link.text());
                    String url = link.absUrl("href");
                    if (url.isBlank() || url.endsWith("/null") || url.equalsIgnoreCase(href)) {
                        continue;
                    }

                    String normalizedLinkText = linkText.toLowerCase();
                    if (normalizedLinkText.contains("spec") || normalizedLinkText.contains("guideline") || url.toLowerCase().endsWith(".pdf")) {
                        specificationUrl = url;
                    } else if (normalizedLinkText.contains("faq") || normalizedLinkText.contains("scheme") && url.toLowerCase().contains("faq")) {
                        faqUrl = url;
                    } else if (normalizedLinkText.contains("apply") || normalizedLinkText.contains("register") || normalizedLinkText.contains("form") || normalizedLinkText.contains("online")) {
                        applicationUrl = url;
                    }
                }

                if (specificationUrl == null) {
                    specificationUrl = findEmbeddedDocumentUrl(container);
                }

                String description = details;
                if (specificationUrl != null && specificationUrl.toLowerCase().endsWith(".pdf")) {
                    try {
                        description += "\n\nPDF DETAILS:\n" + pdfScholarshipExtractor.extractText(specificationUrl);
                    } catch (Exception ignored) {
                        // Keep fallback HTML details if the PDF file is not accessible.
                    }
                }

                Scholarship scholarship = scholarshipRepository.findByNormalizedName(title)
                    .stream()
                    .findFirst()
                    .orElseGet(Scholarship::new);

                scholarship.setName(title);
                scholarship.setDescription(mergeDescription(scholarship.getDescription(), description));
                scholarship.setProvider(source.getName());
                scholarship.setSourceUrl(listingUrl);
                if (applicationUrl != null && !applicationUrl.isBlank()) {
                    scholarship.setApplicationUrl(applicationUrl);
                }
                if (specificationUrl != null && !specificationUrl.isBlank()) {
                    scholarship.setSpecificationUrl(specificationUrl);
                }
                if (faqUrl != null && !faqUrl.isBlank()) {
                    scholarship.setFaqUrl(faqUrl);
                }
                LocalDate applicationStartDate = extractDate(details, "Scheme Open from");
                if (applicationStartDate != null) {
                    scholarship.setApplicationStartDate(applicationStartDate);
                }
                LocalDate applicationDeadline = extractDate(details, "Student Application Open till");
                if (applicationDeadline != null) {
                    scholarship.setApplicationDeadline(applicationDeadline);
                }
                scholarship.setActive(true);
                scholarship.setSource(source);
                scholarshipRepository.save(scholarship);
                scholarships.put(title, scholarship);
            }

            return "Dynamic NSP scraping completed. Scholarships processed: " + scholarships.size();
        } catch (Exception e) {
            return "NSP scraping failed: " + e.getMessage();
        }
    }

    static boolean isLikelyScholarshipLink(String text, String href) {
        String normalizedText = text.toLowerCase();
        String normalizedHref = href.toLowerCase();
        boolean textMatch = normalizedText.contains("scholarship")
                || normalizedText.contains("scheme")
                || normalizedText.contains("fellowship")
                || normalizedText.contains("award")
                || normalizedText.contains("studentship");
        boolean hrefMatch = normalizedHref.contains("scholarship")
                || normalizedHref.contains("scheme")
                || normalizedHref.contains("fellowship")
                || normalizedHref.contains("award");
        return textMatch || hrefMatch;
    }

    private String resolveUsableListingUrl(String baseUrl) {
        List<String> urls = new ArrayList<>();
        String normalizedBaseUrl = baseUrl == null ? "" : baseUrl.trim();
        if (!normalizedBaseUrl.isBlank()) {
            urls.add(normalizedBaseUrl);
            for (String path : CANDIDATE_PATHS) {
                urls.add(normalizedBaseUrl.replaceAll("/?$", "") + path);
            }
        }

        for (String url : urls) {
            try {
                Document doc = fetchDocument(url);
                if (doc != null && !doc.select("a[href]").isEmpty()) {
                    return url;
                }
            } catch (Exception ignored) {
                // Try the next candidate URL.
            }
        }

        return null;
    }

    private Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://www.google.com")
                    .followRedirects(true)
                    .timeout(20000)
                    .ignoreHttpErrors(true)
                    .get();
        } catch (Exception ex) {
            return null;
        }
    }

    private String findEmbeddedDocumentUrl(Element container) {
        if (container == null) {
            return null;
        }

        for (Element link : container.select("a[href]")) {
            String url = link.absUrl("href");
            if (url.toLowerCase().endsWith(".pdf") || url.toLowerCase().contains("pdf") || url.toLowerCase().contains("doc")) {
                return url;
            }
        }
        return null;
    }

    private String sanitizeTitle(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replaceAll("\\s+", " ").trim();
        cleaned = cleaned.replaceAll("\\|.*", "").trim();
        return cleaned;
    }

    private String mergeDescription(String existing, String incoming) {
        String previous = existing == null ? "" : existing.replaceAll("\\s+", " ").trim();
        String latest = incoming == null ? "" : incoming.replaceAll("\\s+", " ").trim();
        if (previous.isBlank()) {
            return limitDescription(latest);
        }
        if (latest.isBlank() || previous.contains(latest)) {
            return limitDescription(previous);
        }
        if (latest.contains(previous)) {
            return limitDescription(latest);
        }
        return limitDescription(previous + "\n\n" + latest);
    }

    private String limitDescription(String description) {
        return description.length() <= 2000 ? description : description.substring(0, 2000);
    }

    private LocalDate extractDate(String details, String label) {
        Pattern pattern = Pattern.compile(
                Pattern.quote(label) + "(?: \\(for Renewal\\))?\\s*:\\s*(\\d{2}-\\d{2}-\\d{4})"
        );
        Matcher matcher = pattern.matcher(details);
        if (matcher.find()) {
            try {
                return LocalDate.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            } catch (Exception ignored) {
                // Ignore invalid dates and fall back to the next matching value.
            }
        }
        return null;
    }
}
