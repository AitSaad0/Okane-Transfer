// ResetPasswordRequestDTO.java
package com.okane.dto.requestDto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResetPasswordRequestDTO {
    private String token;
    private String newPassword;
}