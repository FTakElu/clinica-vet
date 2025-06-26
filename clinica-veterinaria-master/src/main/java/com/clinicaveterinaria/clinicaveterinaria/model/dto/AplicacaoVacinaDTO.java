package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplicacaoVacinaDTO {
    private Long id;
    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;
    private String petNome; // Para exibir no frontend
    @NotNull(message = "ID do tipo de vacina é obrigatório")
    private Long tipoVacinaId;
    private String tipoVacinaNome; // Para exibir no frontend
    private Long veterinarioResponsavelId; // Opcional, se não for obrigatório
    private String veterinarioResponsavelNome; // Para exibir no frontend
    @NotNull(message = "Data de aplicação é obrigatória")
    private LocalDate dataAplicacao;
    private LocalDate dataProximaAplicacao;
    private String loteVacina;
    private String observacoes;
}


