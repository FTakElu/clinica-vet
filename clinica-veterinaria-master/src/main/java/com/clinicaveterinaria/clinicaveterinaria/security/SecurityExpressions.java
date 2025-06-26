package com.clinicaveterinaria.clinicaveterinaria.security;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException; // Apenas para re-usar a exceção se quiser lançar, mas o método principal não lança
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import com.clinicaveterinaria.clinicaveterinaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("securityExpressions") // O nome 'securityExpressions' é o que você usa no @PreAuthorize
public class SecurityExpressions {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Verifica se um SECRETARIO pode modificar (editar/deletar) um usuário com o dado ID.
     * Um secretario pode modificar Cliente, Veterinario ou outro Secretario, mas não um Admin.
     * @param userId O ID do usuário que está sendo alvo da operação.
     * @return true se o secretário tem permissão, false caso contrário.
     */
    public boolean canSecretaryModifyUser(Long userId) {
        // Tenta encontrar o usuário alvo. Se não encontrado, um secretário não pode modificá-lo (retorna false).
        // Evita lançar ResourceNotFoundException diretamente daqui, pois PreAuthorize espera boolean.
        Usuario targetUser = usuarioRepository.findById(userId).orElse(null);

        if (targetUser == null) {
            return false; // Usuário não encontrado, secretário não pode modificar
        }

        // Um Secretário NÃO pode modificar um ADMIN.
        // Retorna true se o usuário alvo NÃO for um ADMIN.
        return targetUser.getRole() != UsuarioRole.ADMIN;
    }
}