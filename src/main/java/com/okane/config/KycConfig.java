package com.okane.config;

import com.okane.kyc.repository.KycAlertRepository;
import com.okane.kyc.repository.WatchlistEntryRepository;
import com.okane.kyc.service.KycService;
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