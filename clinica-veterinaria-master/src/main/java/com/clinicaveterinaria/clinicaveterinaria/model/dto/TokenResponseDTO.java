package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {
    private String token;
    private String userRole;
    private Long userId;   // NOVO CAMPO
    private String userName; // NOVO CAMPO
}
