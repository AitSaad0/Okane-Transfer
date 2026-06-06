package com.okane.dto.requestDto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthRequestDTO {
    private String email;
    private String password;
}