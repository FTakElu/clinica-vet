package com.clinicaveterinaria.clinicaveterinaria.security;

import com.clinicaveterinaria.clinicaveterinaria.model.entity.Pet;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario; // Ou a interface UserDetails que seu principal implementa
import com.clinicaveterinaria.clinicaveterinaria.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("petSecurityExpressions") // Nome usado no @PreAuthorize
public class PetSecurityExpressions {

    @Autowired
    private PetRepository petRepository;

    /**
     * Verifica se o usuário logado é o dono do Pet com o dado ID.
     * @param petId O ID do pet.
     * @return true se o usuário logado é o dono, false caso contrário.
     */
    public boolean isOwner(Long petId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario)) {
            return false; // Não logado ou principal não é uma instância de Usuario
        }

        Usuario currentUser = (Usuario) authentication.getPrincipal();
        if (currentUser.getId() == null) {
            return false; // Usuário logado sem ID (deve ter ID)
        }

        Pet pet = petRepository.findById(petId).orElse(null);
        if (pet == null || pet.getDono() == null) {
            return false; // Pet não encontrado ou sem dono associado
        }

        // Verifica se o ID do dono do pet é igual ao ID do usuário logado
        return pet.getDono().getId().equals(currentUser.getId());
    }
}