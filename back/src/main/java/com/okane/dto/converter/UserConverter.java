package com.okane.dto.converter;

import com.okane.entity.User;
import com.okane.dto.responseDto.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

        public UserResponseDTO toResponseDto(User user) {

            UserResponseDTO dto = new UserResponseDTO();

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
