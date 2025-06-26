package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id;
    @NotBlank @Email private String email;
    @NotBlank private String senha;
    @NotBlank private String nomeCompleto; // Mantido para comunicação frontend
    private String telefone;
    private String cpf;
}
