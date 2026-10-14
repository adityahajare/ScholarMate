package com.scholarmate.scraper;

import com.scholarmate.entity.ScholarshipSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScholarshipScraperRegistryTest {

    @Test
    void selectsDedicatedNspScraperForNspSourceKey() {
        ScholarshipScraper genericScraper = mock(ScholarshipScraper.class);
        ScholarshipScraper nspScraper = mock(ScholarshipScraper.class);
        when(genericScraper.getScraperKey()).thenReturn("GENERIC");
        when(nspScraper.getScraperKey()).thenReturn("NSP");
        ScholarshipSource source = ScholarshipSource.builder()
                .name("National Scholarship Portal (NSP)")
                .scraperKey("NSP")
                .build();

        ScholarshipScraper selected = new ScholarshipScraperRegistry(
                List.of(genericScraper, nspScraper)
        ).getScraper(source);

        assertSame(nspScraper, selected);
    }
}