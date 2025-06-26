package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipos_vacina")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoVacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome; // Ex: Raiva, V8, Gripe
    private String descricao;
    private int periodoReforcoEmMeses; // Ex: 12 (para anual), 6 (para semestral)
}
