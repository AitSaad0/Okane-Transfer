// ChangePasswordRequestDTO.java
package com.okane.dto.requestDto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequestDTO {
    private String currentPassword;
    private String newPassword;
}