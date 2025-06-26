// src/main/java/com/clinicaveterinaria/clinicaveterinaria/model/dto/UsuarioUpdateDTO.java
package com.clinicaveterinaria.clinicaveterinaria.model.dto;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole; // Se você permitir mudança de role aqui
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UsuarioUpdateDTO {
    // Campos que podem ser atualizados
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Nome completo é obrigatório")
    private String nomeCompleto;

    // Senha é opcional para atualização - só é enviada se o usuário quiser mudar
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") // Exemplo de validação
    private String senha;

    // Inclua outros campos que você permite atualizar para diferentes tipos de usuário
    private String telefone; // Para Cliente
    private String cpf;      // Para Cliente
    private String crmv;     // Para Veterinário
    private String especialidade; // Para Veterinário
    private String endereco; // Se Cliente/Veterinário tiverem endereço

    // Se você permitir que ADMIN mude a role de um usuário via update, inclua aqui.
    // private UsuarioRole role;
}