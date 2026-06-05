package com.okane.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService service;

    @BeforeEach
    void setUp() {
        service = new RateLimiterService();
    }

    @Test
    void shouldAllowRequestsUnderLimit() {
        for (int i = 0; i < 10; i++) {
            assertTrue(service.isAllowed("client-1"));
        }
    }

    @Test
    void shouldBlockAfterLimitExceeded() {
        for (int i = 0; i < 10; i++) {
            service.isAllowed("client-2");
        }
        assertFalse(service.isAllowed("client-2"));
    }

    @Test
    void shouldIsolateDifferentClients() {
        for (int i = 0; i < 10; i++) {
            service.isAllowed("client-A");
        }
        assertTrue(service.isAllowed("client-B"));
    }
}