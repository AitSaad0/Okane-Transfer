package com.okane.filter;

import com.okane.security.RateLimitFilter;
import com.okane.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitFilterTest {

    private MockMvc mockMvc;

    // dummy controller just for testing
    @RestController
    static class DummyController {
        @GetMapping("/api/test")
        public String test() { return "ok"; }
    }

    @BeforeEach
    void setUp() {
        RateLimiterService service = new RateLimiterService();
        RateLimitFilter filter = new RateLimitFilter(service);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DummyController())
                .addFilter(filter)
                .build();
    }

    @Test
    void shouldAllowFirst10Requests() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/test"))
                   .andExpect(status().isOk());
        }
    }

    @Test
    void shouldBlock11thRequest() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/test"));
        }
        mockMvc.perform(get("/api/test"))
               .andExpect(status().isTooManyRequests()); // 429
    }
}