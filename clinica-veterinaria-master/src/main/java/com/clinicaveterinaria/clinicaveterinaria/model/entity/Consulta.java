package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.StatusConsulta; // Criaremos este Enum
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Column(columnDefinition = "TEXT") // Para armazenar relatórios maiores
    private String diagnostico; // Resultado da consulta
    @Column(columnDefinition = "TEXT")
    private String tratamento; // Tratamento aplicado
    @Column(columnDefinition = "TEXT")
    private String observacoes; // Observações gerais

    // Relatório pode ser um campo da consulta que só é preenchido após a finalização
    // Ou uma entidade separada se o relatório for muito complexo
}
