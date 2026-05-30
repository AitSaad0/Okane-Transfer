package com.okane.dto.requestDto;

import com.okane.entity.User;
import com.okane.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/2fa")
public class OtpController {
    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }


    @PostMapping("/send")
    public ResponseEntity<Void> send(@AuthenticationPrincipal UserDetails userDetails){
        Long userId = ((User) userDetails).getId();
        otpService.send(userId);
        return ResponseEntity.ok().build();
    }
}
