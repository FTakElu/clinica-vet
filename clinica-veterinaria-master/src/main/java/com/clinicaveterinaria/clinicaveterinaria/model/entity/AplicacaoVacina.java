package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "aplicacoes_vacina")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplicacaoVacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_vacina_id", nullable = false)
    private TipoVacina tipoVacina; // Qual vacina foi aplicada (Raiva, V8, etc.)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_responsavel_id") // Pode ser nulo se for registrado por secretário
    private Veterinario veterinarioResponsavel; // Quem aplicou a vacina

    @Column(nullable = false)
    private LocalDate dataAplicacao;

    private LocalDate dataProximaAplicacao; // Calculado com base no periodoReforco do TipoVacina
    private String loteVacina;
    private String observacoes;
}


