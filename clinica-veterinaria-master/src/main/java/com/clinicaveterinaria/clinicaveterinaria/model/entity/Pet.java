package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.EspeciePet; // <-- Importar o Enum
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate; // <-- Importar LocalDate
import java.util.List; // <-- Importar List para as coleções

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false) // Coluna da chave estrangeira para o Cliente
    private Cliente dono;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING) // Mapeia o Enum como String no DB
    @Column(nullable = false)
    private EspeciePet especie; // Tipo do campo para o Enum

    private String raca;
    private LocalDate dataNascimento;
    private String cor;
    private String observacoes;

    // Relacionamentos adicionados
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consulta> consultas; // Supondo que você terá uma entidade Consulta

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AplicacaoVacina> aplicacoesVacinas; // Supondo que você terá uma entidade AplicacaoVacina

    // Construtor manual, se necessário (Lombok @AllArgsConstructor já gera um)
    // public Pet(Cliente dono, String nome, EspeciePet especie, String raca, LocalDate dataNascimento, String cor, String observacoes) {
    //     this.dono = dono;
    //     this.nome = nome;
    //     this.especie = especie;
    //     this.raca = raca;
    //     this.dataNascimento = dataNascimento;
    //     this.cor = cor;
    //     this.observacoes = observacoes;
    // }
}