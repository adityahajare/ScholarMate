package com.scholarmate.scraper;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class MahaDbtScholarshipScraper implements ScholarshipScraper {

    private static final String MAHADBT_URL =
            "https://mahadbt.maharashtra.gov.in/";

    private final ScholarshipRepository scholarshipRepository;

    public MahaDbtScholarshipScraper(
            ScholarshipRepository scholarshipRepository) {

        this.scholarshipRepository = scholarshipRepository;
    }

    @Override
    public String getScraperKey() {
        return "MAHADBT";
    }

    @Override
    public String scrape(ScholarshipSource source) {

        int discovered = 0;
        int saved = 0;
        int failed = 0;

        try {

            Document homePage = Jsoup.connect(MAHADBT_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(20000)
                    .get();

            Elements links =
                    homePage.select("a[href*='SchemeData']");

            discovered = links.size();

            for (Element link : links) {

                String name = link.text().replaceAll("\\s+", " ").trim();
                String url = link.absUrl("href");

                if (name.isEmpty() || url.isEmpty()) {
                    continue;
                }

                try {

                    /*
                     * STEP 1
                     * Save the scheme immediately.
                     *
                     * This guarantees that a temporary
                     * detail-page failure does not remove
                     * the scheme from our database.
                     */

                    Scholarship scholarship =
                            scholarshipRepository.findByNormalizedName(name)
                                    .stream()
                                    .findFirst()
                                    .orElseGet(Scholarship::new);

                    scholarship.setName(name);
                    scholarship.setSourceUrl(url);
                    scholarship.setActive(true);
                    scholarship.setSource(source);

                    scholarshipRepository.save(scholarship);

                    /*
                     * STEP 2
                     * Try to collect detailed information.
                     */

                    try {

                        Document schemePage =
                                Jsoup.connect(url)
                                        .userAgent("Mozilla/5.0")
                                        .timeout(10000)
                                        .followRedirects(true)
                                        .ignoreHttpErrors(true)
                                        .get();

                        String department =
                                extractSection(
                                        schemePage,
                                        "Department Name");

                        String overview =
                                extractSection(
                                        schemePage,
                                        "Overview");

                        String benefits =
                                extractSection(
                                        schemePage,
                                        "Benefits");

                        String eligibility =
                                extractSection(
                                        schemePage,
                                        "Eligibility");

                        String renewal =
                                extractSection(
                                        schemePage,
                                        "Renewal Policy");

                        String documents =
                                extractSection(
                                        schemePage,
                                        "Documents Required");

                        String description =
                                buildDescription(
                                        department,
                                        overview,
                                        benefits,
                                        eligibility,
                                        renewal,
                                        documents);

                        if (!description.isBlank()) {

                            scholarship.setDescription(
                                    limitDescription(description));

                            scholarshipRepository.save(scholarship);
                        }

                    } catch (Exception detailError) {

                        System.out.println(
                                "Detail extraction failed: "
                                        + name
                                        + " → "
                                        + detailError.getMessage());
                    }

                    saved++;

                    Thread.sleep(250);

                } catch (Exception e) {

                    failed++;

                    System.out.println(
                            "MahaDBT scheme failed: "
                                    + name
                                    + " → "
                                    + e.getMessage());
                }
            }

            return
                    "MahaDBT scraping completed."
                            + " Discovered: " + discovered
                            + ", Saved: " + saved
                            + ", Failed: " + failed;

        } catch (Exception e) {

            return "MahaDBT scraping failed: "
                    + e.getMessage();
        }
    }

    private String extractSection(
            Document document,
            String sectionName) {

        Elements headings =
                document.select("h1, h2, h3, h4, h5, h6");

        for (Element heading : headings) {

            String headingText =
                    heading.text()
                            .trim()
                            .replaceAll("\\s+", " ");

            if (!headingText.equalsIgnoreCase(sectionName)) {
                continue;
            }

            StringBuilder result =
                    new StringBuilder();

            Element current =
                    heading.nextElementSibling();

            while (current != null) {

                if (current.is("h1, h2, h3, h4, h5, h6")) {
                    break;
                }

                String text =
                        current.text()
                                .trim()
                                .replaceAll("\\s+", " ");

                if (!text.isEmpty()) {

                    result.append(text)
                            .append("\n");
                }

                current =
                        current.nextElementSibling();
            }

            return result.toString().trim();
        }

        return "";
    }

    private String buildDescription(
            String department,
            String overview,
            String benefits,
            String eligibility,
            String renewal,
            String documents) {

        return
                "DEPARTMENT:\n"
                        + department
                        + "\n\n"

                        + "OVERVIEW:\n"
                        + overview
                        + "\n\n"

                        + "BENEFITS:\n"
                        + benefits
                        + "\n\n"

                        + "ELIGIBILITY:\n"
                        + eligibility
                        + "\n\n"

                        + "RENEWAL POLICY:\n"
                        + renewal
                        + "\n\n"

                        + "DOCUMENTS REQUIRED:\n"
                        + documents;
    }

    private String limitDescription(String text) {

        if (text == null) {
            return null;
        }

        if (text.length() <= 2000) {
            return text;
        }

        return text.substring(0, 2000);
    }
}