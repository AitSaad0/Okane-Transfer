package com.okane.network_users.controller.converter;

import com.okane.network_users.bean.User;
import com.okane.network_users.controller.dto.responseDto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {
    public UserResponseDto toResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();

        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setTelephone(user.getTelephone());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setAgenceId(user.getAgence()   != null ? user.getAgence().getId()  : null);
        dto.setAgenceNom(user.getAgence()  != null ? user.getAgence().getNom() : null);

        return dto;
    }

}
