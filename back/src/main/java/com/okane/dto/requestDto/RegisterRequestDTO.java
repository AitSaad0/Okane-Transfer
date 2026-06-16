package com.okane.dto.requestDto;

import com.okane.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String telephone;
    private String numPieceIdentite;   // new
    }