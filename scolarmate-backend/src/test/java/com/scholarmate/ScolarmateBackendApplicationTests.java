package com.scholarmate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "scholarmate.scraper.schedule.enabled=false")
class ScolarmateBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
