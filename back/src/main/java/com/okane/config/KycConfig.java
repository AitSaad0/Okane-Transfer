package com.okane.config;

import com.okane.repository.KycAlertRepository;
import com.okane.repository.WatchlistEntryRepository;
import com.okane.service.KycService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KycConfig {

    @Bean
    public KycService kycService(WatchlistEntryRepository watchlistRepo,
                                 KycAlertRepository alertRepo) {
        return new KycService(watchlistRepo, alertRepo);
    }
}