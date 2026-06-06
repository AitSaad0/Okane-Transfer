package com.okane.service;

import org.springframework.stereotype.Service;

@Service
public interface OtpService {
    public void send(Long userId);
    public void verify(Long userId, String valeur);
}
