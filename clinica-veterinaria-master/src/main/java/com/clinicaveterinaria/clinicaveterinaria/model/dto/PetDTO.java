package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.EspeciePet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetDTO {
    private Long id;
    @NotNull(message = "ID do dono (cliente) é obrigatório")
    private Long donoId; // Para associar ao cliente
    @NotBlank(message = "Nome do pet é obrigatório")
    private String nome;
    @NotNull(message = "Espécie do pet é obrigatória")
    private EspeciePet especie;
    private String raca;
    private LocalDate dataNascimento;
    private String cor;
    private String observacoes;
}