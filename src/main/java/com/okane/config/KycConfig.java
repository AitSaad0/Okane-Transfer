package com.okane.config;

import com.okane.repository.kyc.KycAlertRepository;
import com.okane.repository.kyc.WatchlistEntryRepository;
import com.okane.service.kyc.KycService;
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