package com.scholarmate.scraper;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenericWebsiteScholarshipScraperTest {

    @Test
    void fingerprintChangesWhenOneCharacterChanges() {
        String originalHash = GenericWebsiteScholarshipScraper.fingerprint("Award amount: 1000");
        String changedHash = GenericWebsiteScholarshipScraper.fingerprint("Award amount: 1001");

        assertNotEquals(originalHash, changedHash);
        assertEquals(originalHash, GenericWebsiteScholarshipScraper.fingerprint("Award amount: 1000"));
    }

    @Test
    void sparseRefreshPreservesPreviouslyCollectedUrlsAndDescription() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> respond(exchange,
            "<a href='/details/merit'>State Merit Scholarship</a>"));
        server.createContext("/details/merit", exchange -> respond(exchange,
            "<h1>State Merit Scholarship</h1><p>Updated listing details.</p>"));
        server.start();

        try {
            ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
            PdfScholarshipExtractor pdfExtractor = mock(PdfScholarshipExtractor.class);
            Scholarship existing = Scholarship.builder()
                .id(77L)
                .name("State Merit Scholarship")
                .applicationUrl("https://official.example/apply")
                .specificationUrl("https://official.example/guidelines.pdf")
                .description("Previously verified eligibility details and required documents.")
                .build();
            when(scholarshipRepository.findByNormalizedNameAndSourceId(anyString(), eq(77L)))
                .thenReturn(List.of(existing));
            when(scholarshipRepository.save(org.mockito.ArgumentMatchers.any(Scholarship.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            GenericWebsiteScholarshipScraper scraper = new GenericWebsiteScholarshipScraper(
                scholarshipRepository,
                pdfExtractor);
            ScholarshipSource source = ScholarshipSource.builder()
                .id(77L)
                .name("Test scholarship source")
                .baseUrl("http://localhost:" + server.getAddress().getPort() + "/")
                .build();

            scraper.scrape(source);

            assertEquals("https://official.example/apply", existing.getApplicationUrl());
            assertEquals("https://official.example/guidelines.pdf", existing.getSpecificationUrl());
            assertTrue(existing.getDescription().contains(
                "Previously verified eligibility details and required documents."));
        } finally {
            server.stop(0);
        }
    }

        @Test
        void crawlsSameSiteDetailsAndPdfAndRechecksWhenDetailPageChanges() throws Exception {
        AtomicReference<String> detailContent = new AtomicReference<>("Eligibility requires 70 percent marks.");
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> respond(exchange,
            "<a href='/scholarships'>Scholarships</a>"));
        server.createContext("/scholarships", exchange -> respond(exchange,
            "<article><a href='/details/merit'>State Merit Scholarship</a>"
                + "<a href='/apply/merit'>Apply Now</a>"
                + "<a href='/docs/merit.pdf'>Guidelines PDF</a>"
                + "<p>For students in Maharashtra.</p></article>"));
        server.createContext("/details/merit", exchange -> respond(exchange,
            "<h1>State Merit Scholarship</h1><p>" + detailContent.get() + "</p>"));
        server.start();

        try {
            ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
            PdfScholarshipExtractor pdfExtractor = mock(PdfScholarshipExtractor.class);
            List<Scholarship> stored = new ArrayList<>();
            when(scholarshipRepository.findByNormalizedNameAndSourceId(anyString(), eq(77L)))
                .thenAnswer(invocation -> stored.isEmpty() ? List.of() : List.of(stored.getFirst()));
            when(scholarshipRepository.save(org.mockito.ArgumentMatchers.any(Scholarship.class)))
                .thenAnswer(invocation -> {
                Scholarship scholarship = invocation.getArgument(0);
                if (stored.isEmpty()) stored.add(scholarship);
                return scholarship;
                });
            when(pdfExtractor.extractText("http://localhost:" + server.getAddress().getPort() + "/docs/merit.pdf"))
                .thenReturn("Official PDF eligibility: family income below INR 500000.");

            GenericWebsiteScholarshipScraper scraper = new GenericWebsiteScholarshipScraper(
                scholarshipRepository,
                pdfExtractor
            );
            ScholarshipSource source = ScholarshipSource.builder()
                .id(77L)
                .name("Test scholarship source")
                .baseUrl("http://localhost:" + server.getAddress().getPort() + "/")
                .build();

            String firstResult = scraper.scrape(source);
            String firstHash = source.getContentHash();

            assertTrue(firstResult.contains("Pages: 3"));
            assertEquals(1, stored.size());
            assertEquals("State Merit Scholarship", stored.getFirst().getName());
            assertEquals("http://localhost:" + server.getAddress().getPort() + "/apply/merit",
                stored.getFirst().getApplicationUrl());
            assertEquals("http://localhost:" + server.getAddress().getPort() + "/docs/merit.pdf",
                stored.getFirst().getSpecificationUrl());
                assertTrue(stored.getFirst().getDescription().contains(detailContent.get()),
                    stored.getFirst().getDescription());
            assertTrue(stored.getFirst().getDescription().contains("Official PDF eligibility"));

            detailContent.set("Eligibility requires 75 percent marks.");
            String secondResult = scraper.scrape(source);

            assertTrue(secondResult.contains("upserted: 1"));
            assertNotEquals(firstHash, source.getContentHash());
                assertTrue(stored.getFirst().getDescription().contains(detailContent.get()),
                    stored.getFirst().getDescription());
            verify(pdfExtractor, times(2)).extractText(anyString());
        } finally {
            server.stop(0);
        }
        }

        @Test
        void discoversScholarshipFromPdfOnlyListing() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> respond(exchange,
            "<a href='/docs/first-generation-scholarship.pdf'>First Generation Scholarship.pdf</a>"));
        server.start();

        try {
            ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
            PdfScholarshipExtractor pdfExtractor = mock(PdfScholarshipExtractor.class);
            when(scholarshipRepository.findByNormalizedNameAndSourceId(anyString(), eq(77L)))
                .thenReturn(List.of());
            when(scholarshipRepository.save(org.mockito.ArgumentMatchers.any(Scholarship.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
            when(pdfExtractor.extractText("http://localhost:" + server.getAddress().getPort()
                + "/docs/first-generation-scholarship.pdf"))
                .thenReturn("Eligibility details extracted from the official award PDF.");

            ScholarshipSource source = ScholarshipSource.builder()
                .id(77L)
                .name("PDF-only source")
                .baseUrl("http://localhost:" + server.getAddress().getPort() + "/")
                .build();
            GenericWebsiteScholarshipScraper scraper = new GenericWebsiteScholarshipScraper(
                scholarshipRepository,
                pdfExtractor
            );

            String result = scraper.scrape(source);

            assertTrue(result.contains("discovered: 1"));
            verify(scholarshipRepository).save(org.mockito.ArgumentMatchers.argThat(scholarship ->
                "First Generation Scholarship".equals(scholarship.getName())
                    && scholarship.getSpecificationUrl().endsWith("first-generation-scholarship.pdf")
                    && scholarship.getDescription().contains("Eligibility details extracted")));
        } finally {
            server.stop(0);
        }
        }

        private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
        }
}