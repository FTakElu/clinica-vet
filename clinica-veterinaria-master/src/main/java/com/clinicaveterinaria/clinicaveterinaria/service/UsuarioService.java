// src/main/java/com/clinicaveterinaria/clinicaveterinaria/service/UsuarioService.java
package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException; // Certifique-se de ter essa exceção
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioUpdateDTO; // NOVO: Importa o DTO de atualização
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Cliente;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Veterinario;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import com.clinicaveterinaria.clinicaveterinaria.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Para criptografar a senha na atualização
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service // Marca como um componente de serviço do Spring
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injeta o PasswordEncoder para criptografia

    // --- Métodos Auxiliares ---

    // Converte Entidade Usuario para UsuarioDTO
    private UsuarioDTO convertToDto(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setEmail(usuario.getEmail());
        dto.setNomeCompleto(usuario.getNome());
        dto.setRole(usuario.getRole());

        // Popula campos específicos apenas se a instância for do tipo correto
        if (usuario instanceof Cliente) {
            Cliente cliente = (Cliente) usuario;
            dto.setTelefone(cliente.getTelefone());
            dto.setCpf(cliente.getCpf());
            // Se Cliente tem 'endereco', adicione: dto.setEndereco(cliente.getEndereco());
        } else if (usuario instanceof Veterinario) {
            Veterinario veterinario = (Veterinario) usuario;
            dto.setCrmv(veterinario.getCrmv());
            dto.setEspecialidade(veterinario.getEspecialidade());
            // Se Veterinario tem 'telefone' ou 'endereco', adicione:
            // dto.setTelefone(veterinario.getTelefone());
            // dto.setEndereco(veterinario.getEndereco());
        }
        return dto;
    }

    // --- Métodos de Lógica de Negócio para Usuários ---

    // Retorna todos os usuários
    public List<UsuarioDTO> findAllUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Retorna um usuário por ID
    public UsuarioDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return convertToDto(usuario);
    }

    // Atualiza um usuário existente
    public UsuarioDTO updateUser(Long id, UsuarioUpdateDTO usuarioUpdateDTO) { // Usa UsuarioUpdateDTO aqui!
        Usuario existingUser = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        // Atualiza campos comuns
        existingUser.setEmail(usuarioUpdateDTO.getEmail());
        existingUser.setNome(usuarioUpdateDTO.getNomeCompleto());

        // Atualiza a senha SOMENTE se uma nova senha for fornecida no DTO de atualização
        if (usuarioUpdateDTO.getSenha() != null && !usuarioUpdateDTO.getSenha().isEmpty()) {
            existingUser.setSenha(passwordEncoder.encode(usuarioUpdateDTO.getSenha()));
        }

        // Se você permitir a mudança de role por este DTO e o usuário logado tiver permissão (Admin)
        // if (usuarioUpdateDTO.getRole() != null && existingUser.getRole() != usuarioUpdateDTO.getRole()) {
        //     existingUser.setRole(usuarioUpdateDTO.getRole());
        //     // ATENÇÃO: Mudar a role pode exigir mais lógica para mudar a entidade JPA se elas são hierárquicas
        //     // Cliente -> Veterinario pode não ser uma simples mudança de role, mas uma criação/deleção de entidades
        // }


        // Atualiza campos específicos com base no TIPO DO USUÁRIO EXISTENTE
        if (existingUser instanceof Cliente) {
            Cliente cliente = (Cliente) existingUser;
            cliente.setTelefone(usuarioUpdateDTO.getTelefone());
            cliente.setCpf(usuarioUpdateDTO.getCpf());
            // Se Cliente tem 'endereco', adicione: cliente.setEndereco(usuarioUpdateDTO.getEndereco());
        } else if (existingUser instanceof Veterinario) {
            Veterinario veterinario = (Veterinario) existingUser;
            veterinario.setCrmv(usuarioUpdateDTO.getCrmv());
            veterinario.setEspecialidade(usuarioUpdateDTO.getEspecialidade());
            // Se Veterinario tem 'telefone' ou 'endereco', adicione:
            // veterinario.setTelefone(usuarioUpdateDTO.getTelefone());
            // veterinario.setEndereco(usuarioUpdateDTO.getEndereco());
        }

        Usuario updatedUser = usuarioRepository.save(existingUser);
        return convertToDto(updatedUser); // Retorna o DTO convertido
    }

    // Deleta um usuário por ID
    public void deleteUser(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // Retorna todos os clientes
    public List<UsuarioDTO> findAllClients() {
        return usuarioRepository.findByRole(UsuarioRole.CLIENTE).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Retorna todos os veterinários
    public List<UsuarioDTO> findAllVeterinarios() {
        return usuarioRepository.findByRole(UsuarioRole.VETERINARIO).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}