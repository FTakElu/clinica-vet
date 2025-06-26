package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; // Adicionado, se você usa construtor com todos os args
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionado, se você usa construtor sem args

// DTO para o registro de usuários com role específica (usado por ADMIN)
@Data
@NoArgsConstructor // Para desserialização de JSON
@AllArgsConstructor // Útil para testes ou construtores completos
public class UsuarioRegisterDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @NotBlank(message = "Nome completo é obrigatório") // Nome do campo CORRETO
    private String nomeCompleto;

    @NotNull(message = "Role é obrigatória")
    private UsuarioRole role;

    // Campos opcionais (não devem ter @NotBlank se forem opcionais)
    private String crmv;
    private String especialidade;
    private String telefone;
    private String cpf;
    private String endereco; // ADICIONADO: Para corresponder ao que o frontend envia
}
