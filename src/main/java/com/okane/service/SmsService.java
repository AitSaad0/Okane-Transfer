package com.okane.service;

import org.springframework.stereotype.Service;

@Service
public interface SmsService {
    public void sendSms(String phone, String code);
}
