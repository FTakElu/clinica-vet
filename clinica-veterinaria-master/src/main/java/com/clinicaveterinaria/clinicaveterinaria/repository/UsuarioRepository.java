// src/main/java/com/clinicaveterinaria/clinicaveterinaria.repository/UsuarioRepository.java
package com.clinicaveterinaria.clinicaveterinaria.repository;

import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRole(UsuarioRole role); // Este método é necessário agora
}