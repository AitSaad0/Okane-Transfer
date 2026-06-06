package com.okane.config;

import com.okane.security.RateLimitFilter;
import com.okane.service.RateLimiterService;
import jakarta.servlet.Filter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;


public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Filter[] getServletFilters() {
        RateLimiterService rateLimiterService = new RateLimiterService();
        return new Filter[] { new RateLimitFilter(rateLimiterService) };
    }


    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{ AppConfig.class, SecurityConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{ MvcConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{ "/" };
    }
}