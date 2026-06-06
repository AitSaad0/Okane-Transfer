package com.okane.service.impl;

import com.okane.service.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private final String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
    private final String authToken  = System.getenv("TWILIO_AUTH_TOKEN");
    private final String fromNumber = System.getenv("TWILIO_PHONE_NUMBER");

    @Override
    public void sendSms(String toNumber, String message) {
        Twilio.init(accountSid, authToken);
        Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(fromNumber),
                message
        ).create();
    }
}