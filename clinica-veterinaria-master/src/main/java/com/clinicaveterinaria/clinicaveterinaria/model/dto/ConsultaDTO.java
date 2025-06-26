package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.StatusConsulta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaDTO {
    private Long id;
    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;
    private String clienteNome; // Para exibir no frontend
    @NotNull(message = "ID do veterinário é obrigatório")
    private Long veterinarioId;
    private String veterinarioNome; // Para exibir no frontend
    @NotNull(message = "ID do pet é obrigatório")
    private Long petId;
    private String petNome; // Para exibir no frontend
    @NotNull(message = "Data e hora da consulta são obrigatórios")
    private LocalDateTime dataHora;
    private StatusConsulta status; // Será settado pelo backend ou enviado como AGENDADA
    private String diagnostico;
    private String tratamento;
    private String observacoes;
}
