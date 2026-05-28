package com.okane.clients_transfers.controller.converter;

import com.okane.clients_transfers.bean.Client;
import com.okane.clients_transfers.controller.dto.responseDto.ClientProfileResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ClientConverter {

    public ClientProfileResponseDto toProfileDto(Client client) {
        ClientProfileResponseDto dto = ClientProfileResponseDto.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .numPieceIdentite(client.getNumPieceIdentite())
                .dateNaissance(client.getDateNaissance())
                .paysNom(client.getPays() != null ? client.getPays().getNom()     : null)
                .paysCode(client.getPays() != null ? client.getPays().getCodeIso() : null)
                .estSurListeSurveillance(client.getEstSurListeSurveillance())
                .build();

        // préférences depuis User lié
        if (client.getUser() != null) {
            dto.setNotificationEmail(client.getUser().getNotificationEmail());
            dto.setNotificationSms(client.getUser().getNotificationSms());
            dto.setNotificationPush(client.getUser().getNotificationPush());
        }

        return dto;
    }
}