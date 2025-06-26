package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoVacinaDTO {
    private Long id;
    @NotBlank(message = "Nome da vacina é obrigatório")
    private String nome;
    private String descricao;
    @Min(value = 0, message = "Período de reforço deve ser um número não negativo")
    private int periodoReforcoEmMeses;
}
