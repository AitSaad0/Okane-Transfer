package com.okane.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

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
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "countries")
    private List<String> countries;

}