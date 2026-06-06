package com.okane.dto.responseDto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientProfileResponseDto {

        private Long    id;
        private String  nom;
        private String  prenom;
        private String  email;
        private String  telephone;
        private String  numPieceIdentite;
        private LocalDate dateNaissance;
        private String  paysNom;
        private String  paysCode;
        private Boolean estSurListeSurveillance;

        // préférences de notification
        private Boolean notificationEmail;
        private Boolean notificationSms;
        private Boolean notificationPush;

}
