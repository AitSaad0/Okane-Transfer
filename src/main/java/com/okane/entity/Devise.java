package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devise")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "code",nullable = false, unique = true, length = 10)
    private String code;

    @Column(name = "symbole",nullable = false, length = 10)
    private String symbole;

    @Column(name = "nom", nullable = false)
    private String nom;

}