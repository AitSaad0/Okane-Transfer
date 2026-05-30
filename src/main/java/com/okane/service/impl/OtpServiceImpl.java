package com.okane.service.impl;

import com.okane.entity.Token;
import com.okane.entity.User;
import com.okane.entity.enums.TypeToken;
import com.okane.repository.TokenRepository;
import com.okane.repository.UserRepository;
import com.okane.service.OtpService;
import com.okane.service.SmsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {
    private final SmsService smsService;
    private final UserRepository userRepository;
    private final TokenRepository otpRepository;


    public OtpServiceImpl(TokenRepository otpRepository,
                      UserRepository userRepository,
                      SmsService smsService) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.smsService = smsService;
    }


    @Override
    @Transactional
    public void send(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpRepository.deleteByUtilisateurIdAndType(userId, TypeToken.OTP_2FA);

        String code = String.format("%06d",new Random().nextInt(999999));

        Token otp = Token.builder()
                .valeur(code)
                .type(TypeToken.OTP_2FA)
                .dateExpiration(LocalDateTime.now().plusMinutes(5))
                .booleenUtilise(false)
                .utilisateur(user)
                .build();

        otpRepository.save(otp);

        smsService.sendSms(user.getTelephone(), "your OTP is : " + code);
    }
}
