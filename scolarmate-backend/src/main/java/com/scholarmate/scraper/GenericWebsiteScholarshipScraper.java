package com.scholarmate.scraper;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class GenericWebsiteScholarshipScraper implements ScholarshipScraper {

    private static final int MAX_PAGES = 12;
    private static final int MAX_DEPTH = 2;
    private static final int MAX_PDFS_PER_SOURCE = 6;
    private static final int MAX_HTML_BYTES = 2 * 1024 * 1024;
    private static final int MAX_URL_LENGTH = 2048;
    private static final int MAX_DESCRIPTION_LENGTH = 1800;
    private static final long REQUEST_DELAY_MILLIS = 150;
    private static final Pattern CRAWL_PATH = Pattern.compile(
        "(?i)(scholar|scheme|fellow|grant|award|student-aid|financial-aid|education-fund)");
    private static final Set<String> GENERIC_LINK_TITLES = Set.of(
        "scholarship", "scholarships", "schemes", "student schemes", "student scholarships",
        "learn more", "read more", "click here", "apply now", "view details", "more details");

    private final ScholarshipRepository scholarshipRepository;
    private final PdfScholarshipExtractor pdfScholarshipExtractor;

    public GenericWebsiteScholarshipScraper(
        ScholarshipRepository scholarshipRepository,
        PdfScholarshipExtractor pdfScholarshipExtractor) {
        this.scholarshipRepository = scholarshipRepository;
    this.pdfScholarshipExtractor = pdfScholarshipExtractor;
    }

    @Override
    public String getScraperKey() {
        return "GENERIC";
    }

    @Override
    public String scrape(ScholarshipSource source) {
        if (source == null || source.getBaseUrl() == null || source.getBaseUrl().isBlank()) {
            return "No valid source URL configured.";
        }

        try {
            URI baseUri = URI.create(source.getBaseUrl());
            if (baseUri.getHost() == null || baseUri.getScheme() == null
                    || !(baseUri.getScheme().equalsIgnoreCase("http")
                    || baseUri.getScheme().equalsIgnoreCase("https"))) {
                return "Invalid HTTP source URL for " + source.getName() + ".";
            }

            CrawlResult crawl = crawl(source.getBaseUrl(), baseUri);
            if (crawl.pagesVisited == 0) {
                return "Generic scraping failed for " + source.getName() + ": no pages could be fetched.";
            }
            int attemptedPdfs = 0;
            Map<String, String> extractedPdfText = new HashMap<>();
            Set<String> failedPdfUrls = new HashSet<>();
            for (Candidate candidate : crawl.candidates.values()) {
                if (candidate.pdfUrl == null) {
                    continue;
                }
                crawl.snapshot.append("PDF URL: ").append(candidate.pdfUrl).append('\n');
                if (extractedPdfText.containsKey(candidate.pdfUrl)) {
                    candidate.pdfText = extractedPdfText.get(candidate.pdfUrl);
                    crawl.snapshot.append(candidate.pdfText).append('\n');
                    continue;
                }
                if (failedPdfUrls.contains(candidate.pdfUrl)) {
                    continue;
                }
                if (attemptedPdfs >= MAX_PDFS_PER_SOURCE) {
                    crawl.snapshot.append("PDF extraction limit reached\n");
                    continue;
                }
                attemptedPdfs++;
                try {
                    candidate.pdfText = pdfScholarshipExtractor.extractText(candidate.pdfUrl);
                    extractedPdfText.put(candidate.pdfUrl, candidate.pdfText);
                    crawl.snapshot.append(candidate.pdfText).append('\n');
                } catch (Exception e) {
                    failedPdfUrls.add(candidate.pdfUrl);
                    crawl.failed++;
                }
            }

            String contentHash = fingerprint(crawl.snapshot.toString());
            if (contentHash.equals(source.getContentHash()) && crawl.failed == 0) {
                return "No content changes detected for " + source.getName()
                        + ". Pages: " + crawl.pagesVisited;
            }

            int upserted = 0;
            for (Candidate candidate : crawl.candidates.values()) {
                try {
                    String detailText = crawl.pageTextByUrl.getOrDefault(candidate.detailUrl, "");
                    String description = combineEvidence(candidate.context, detailText, candidate.pdfText);
                    if (description.isBlank()) {
                        description = "Discovered on " + source.getName()
                                + "; scholarship details need verification.";
                    }

                    Scholarship scholarship = scholarshipRepository
                            .findByNormalizedNameAndSourceId(candidate.title, source.getId())
                            .stream()
                            .findFirst()
                            .orElseGet(Scholarship::new);

                    scholarship.setName(candidate.title);
                    scholarship.setProvider(source.getName());
                    scholarship.setSourceUrl(source.getBaseUrl());
                    if (candidate.applicationUrl != null && !candidate.applicationUrl.isBlank()) {
                        scholarship.setApplicationUrl(candidate.applicationUrl);
                    }
                    if (candidate.pdfUrl != null && !candidate.pdfUrl.isBlank()) {
                        scholarship.setSpecificationUrl(candidate.pdfUrl);
                    }
                    scholarship.setDescription(mergeDescription(scholarship.getDescription(), description));
                    scholarship.setActive(true);
                    scholarship.setSource(source);
                    scholarshipRepository.save(scholarship);
                    upserted++;
                } catch (Exception e) {
                    crawl.failed++;
                }
            }

            if (crawl.failed == 0) {
                source.setContentHash(contentHash);
            }
            return "Generic scraping completed for " + source.getName()
                    + ". Pages: " + crawl.pagesVisited
                    + ", discovered: " + crawl.candidates.size()
                    + ", upserted: " + upserted
                    + ", failed pages/records: " + crawl.failed;
        } catch (Exception e) {
            return "Generic scraping failed for " + source.getName() + ": " + e.getMessage();
        }
    }

    private CrawlResult crawl(String startUrl, URI origin) {
        CrawlResult result = new CrawlResult();
        Deque<CrawlPage> pending = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        pending.add(new CrawlPage(startUrl, 0));

        while (!pending.isEmpty() && visited.size() < MAX_PAGES) {
            CrawlPage page = pending.removeFirst();
            String pageUrl = canonicalUrl(page.url);
            if (pageUrl == null || !visited.add(pageUrl)) {
                continue;
            }

            if (result.pagesVisited > 0 && !waitBeforeRequest(result)) {
                break;
            }

            try {
                Connection.Response response = Jsoup.connect(pageUrl)
                        .userAgent("Mozilla/5.0 (compatible; ScholarMateBot/1.0)")
                        .followRedirects(true)
                        .ignoreHttpErrors(true)
                        .timeout(12000)
                        .maxBodySize(MAX_HTML_BYTES)
                        .execute();
                if (response.statusCode() >= 400) {
                    result.failed++;
                    continue;
                }

                Document document = response.parse();
                result.pagesVisited++;
                result.snapshot.append(pageUrl).append('\n').append(document.outerHtml()).append('\n');
                result.pageTextByUrl.put(pageUrl, limit(document.body() == null ? "" : document.body().text()));

                for (Element link : document.select("a[href]")) {
                    String href = canonicalUrl(link.absUrl("href"));
                    if (href == null || href.length() > MAX_URL_LENGTH) {
                        continue;
                    }

                    String text = cleanText(link.text());
                    Element container = link.closest("article, li, tr, section, div");
                    if (container == null) {
                        container = link.parent();
                    }
                    String context = container == null ? text : limit(cleanText(container.text()));

                    if (isScholarshipCandidate(text, href)) {
                        Candidate candidate = candidateFor(link, container, text, href, context, origin);
                        if (candidate != null) {
                            String key = normalizeTitle(candidate.title) + "|" + candidate.detailUrl;
                            Candidate existing = result.candidates.get(key);
                            if (existing == null) {
                                result.candidates.put(key, candidate);
                            } else {
                                existing.merge(candidate);
                            }
                        }
                    }

                    if (!isPdfUrl(href) && page.depth < MAX_DEPTH && sameOrigin(href, origin)
                            && (isScholarshipCandidate(text, href) || isRelevantPath(href))) {
                        pending.addLast(new CrawlPage(href, page.depth + 1));
                    }
                }
            } catch (Exception e) {
                result.failed++;
            }
        }
        return result;
    }

    private Candidate candidateFor(
            Element candidateLink,
            Element container,
            String text,
            String href,
            String context,
            URI origin) {
        String title = text;
        if (isPdfUrl(href)) {
            title = title.replaceFirst("(?i)\\.pdf$", "")
                .replaceFirst("(?i)\\s+(guidelines?|documents?|download)$", "")
                .trim();
        }
        if (isGenericTitle(title)) {
            title = container == null ? "" : cleanText(container.select("h1, h2, h3, h4, h5, h6").text());
        }
        if (title.isBlank() || isGenericTitle(title)) {
            return null;
        }

        String applicationUrl = null;
        String pdfUrl = isPdfUrl(href) ? href : null;
        if (container != null) {
            for (Element relatedLink : container.select("a[href]")) {
                String relatedUrl = canonicalUrl(relatedLink.absUrl("href"));
                if (relatedUrl == null || relatedUrl.length() > MAX_URL_LENGTH) {
                    continue;
                }
                String relatedText = cleanText(relatedLink.text()).toLowerCase(Locale.ROOT);
                String relatedValue = relatedUrl.toLowerCase(Locale.ROOT);
                if (sameOrigin(relatedUrl, origin) && isPdfUrl(relatedUrl)
                        && (pdfUrl == null || hasScholarshipLanguage(relatedText + " " + relatedValue))) {
                    pdfUrl = relatedUrl;
                }
                if (applicationUrl == null && (relatedText.matches(".*\\b(apply|application|register|registration)\\b.*")
                        || relatedValue.contains("apply") || relatedValue.contains("application-form"))) {
                    applicationUrl = relatedUrl;
                }
            }
        }

        String detailUrl = isPdfUrl(href) ? "" : href;
        if (detailUrl.isBlank() && candidateLink != null) {
            detailUrl = href;
        }
        return new Candidate(title, detailUrl, applicationUrl, pdfUrl, context);
    }

    private boolean isScholarshipCandidate(String text, String href) {
        String combined = (text + " " + href).toLowerCase(Locale.ROOT);
        return hasScholarshipLanguage(combined) && !isGenericTitle(text);
    }

    private boolean hasScholarshipLanguage(String value) {
        return value.contains("scholarship") || value.contains("fellowship")
                || value.contains("studentship") || value.contains("financial-aid")
                || value.contains("student-grant") || value.contains("student-award");
    }

    private boolean isRelevantPath(String url) {
        try {
            return CRAWL_PATH.matcher(URI.create(url).getPath()).find();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isGenericTitle(String text) {
        return GENERIC_LINK_TITLES.contains(normalizeTitle(text));
    }

    private boolean isPdfUrl(String url) {
        return URI.create(url).getPath().toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    private boolean sameOrigin(String url, URI origin) {
        try {
            URI target = URI.create(url);
            return origin.getScheme().equalsIgnoreCase(target.getScheme())
                    && origin.getHost().equalsIgnoreCase(target.getHost())
                    && effectivePort(origin) == effectivePort(target);
        } catch (Exception e) {
            return false;
        }
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private String canonicalUrl(String url) {
        try {
            URI uri = URI.create(url).normalize();
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            return new URI(uri.getScheme().toLowerCase(Locale.ROOT), uri.getUserInfo(),
                    uri.getHost().toLowerCase(Locale.ROOT), uri.getPort(), uri.getPath(),
                    uri.getQuery(), null).toASCIIString();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean waitBeforeRequest(CrawlResult result) {
        try {
            Thread.sleep(REQUEST_DELAY_MILLIS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.failed++;
            return false;
        }
    }

    private String cleanText(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private String normalizeTitle(String value) {
        return cleanText(value).toLowerCase(Locale.ROOT);
    }

    private String combineEvidence(String... sections) {
        StringBuilder description = new StringBuilder();
        for (String section : sections) {
            String text = cleanText(section);
            if (!text.isBlank() && !description.toString().contains(text)) {
                if (description.length() > 0) {
                    description.append("\n\n");
                }
                description.append(text);
            }
        }
        return description.toString();
    }

    private String mergeDescription(String existing, String incoming) {
        String previous = cleanText(existing);
        String latest = cleanText(incoming);
        if (previous.isBlank()) {
            return limit(latest);
        }
        if (latest.isBlank() || previous.contains(latest)) {
            return limit(previous);
        }
        if (latest.contains(previous)) {
            return limit(latest);
        }
        return limit(previous + "\n\n" + latest);
    }

    private String limit(String text) {
        if (text == null || text.length() <= MAX_DESCRIPTION_LENGTH) {
            return text == null ? "" : text;
        }
        return text.substring(0, MAX_DESCRIPTION_LENGTH);
    }

    static String fingerprint(String content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private record CrawlPage(String url, int depth) {
    }

    private static final class Candidate {
        private final String title;
        private final String detailUrl;
        private String applicationUrl;
        private String pdfUrl;
        private String context;
        private String pdfText = "";

        private Candidate(String title, String detailUrl, String applicationUrl, String pdfUrl, String context) {
            this.title = title;
            this.detailUrl = detailUrl;
            this.applicationUrl = applicationUrl;
            this.pdfUrl = pdfUrl;
            this.context = context;
        }

        private void merge(Candidate other) {
            if (applicationUrl == null) applicationUrl = other.applicationUrl;
            if (pdfUrl == null) pdfUrl = other.pdfUrl;
            if (other.context.length() > context.length()) context = other.context;
        }
    }

    private static final class CrawlResult {
        private final StringBuilder snapshot = new StringBuilder();
        private final Map<String, String> pageTextByUrl = new LinkedHashMap<>();
        private final Map<String, Candidate> candidates = new LinkedHashMap<>();
        private int pagesVisited;
        private int failed;
    }
}
