package com.scholarmate.scraper;

import com.scholarmate.entity.Scholarship;
import com.scholarmate.entity.ScholarshipSource;
import com.scholarmate.repository.ScholarshipRepository;
import com.scholarmate.repository.ScholarshipSourceRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NspScholarshipScraperTest {

    @Test
    void shouldRecognizeScholarshipTitlesAndLinks() {
        assertTrue(NspScholarshipScraper.isLikelyScholarshipLink(
                "National Scholarship Scheme",
                "https://scholarships.gov.in/scholarship-details"
        ));

        assertTrue(NspScholarshipScraper.isLikelyScholarshipLink(
                "Post Matric Scholarship",
                "https://example.com/post-matric"
        ));

        assertFalse(NspScholarshipScraper.isLikelyScholarshipLink(
                "Home Page",
                "https://example.com/home"
        ));
    }

    @Test
    void sparseRefreshPreservesPreviouslyCollectedUrlsAndDescription() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> respond(exchange,
                "<a href='/scholarship/merit'>Merit Scholarship</a>"));
        server.start();

        try {
            ScholarshipRepository scholarshipRepository = mock(ScholarshipRepository.class);
            ScholarshipSourceRepository sourceRepository = mock(ScholarshipSourceRepository.class);
            PdfScholarshipExtractor pdfExtractor = mock(PdfScholarshipExtractor.class);
            Scholarship existing = Scholarship.builder()
                    .id(77L)
                    .name("Merit Scholarship")
                    .applicationUrl("https://official.example/apply")
                    .specificationUrl("https://official.example/guidelines.pdf")
                    .description("Previously verified eligibility details and required documents.")
                    .build();
            when(scholarshipRepository.findByNormalizedName("Merit Scholarship"))
                    .thenReturn(List.of(existing));
            when(scholarshipRepository.save(any(Scholarship.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            NspScholarshipScraper scraper = new NspScholarshipScraper(
                    scholarshipRepository,
                    sourceRepository,
                    pdfExtractor);
            ScholarshipSource source = ScholarshipSource.builder()
                    .id(77L)
                    .name("NSP test source")
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

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
