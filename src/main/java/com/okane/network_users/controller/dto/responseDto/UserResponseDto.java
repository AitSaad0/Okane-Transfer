package com.okane.network_users.controller.dto.responseDto;

import com.okane.shared.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long    id;
    private String  email;
    private String  nom;
    private String  prenom;
    private String  telephone;
    private Role    role;
    private Boolean active;
    private Long    agenceId;
    private String  agenceNom;
}

