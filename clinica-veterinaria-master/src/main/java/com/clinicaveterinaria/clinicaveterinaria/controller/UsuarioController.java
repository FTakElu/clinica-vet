package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioUpdateDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // GET /api/usuarios
    // Admins e Secretarios podem listar todos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.findAllUsers();
        return ResponseEntity.ok(usuarios);
    }

    // GET /api/usuarios/{id}
    // Admins, Secretarios ou o próprio usuário podem ver o perfil
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO') or (authentication.principal.id == #id)")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long id) {
        UsuarioDTO usuario = usuarioService.findById(id);
        return ResponseEntity.ok(usuario);
    }

    // PUT /api/usuarios/{id}
    // Admin pode editar qualquer um.
    // Secretario pode editar Cliente, Veterinario, ou outro Secretario.
    // O próprio usuário (Cliente/Veterinario/Secretario) pode editar a si mesmo.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') " +
            "or (hasRole('SECRETARIO') and @securityExpressions.canSecretaryModifyUser(#id)) " +
            "or (authentication.principal.id == #id)")
    public ResponseEntity<UsuarioDTO> updateUsuario(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateDTO usuarioUpdateDTO) {
        UsuarioDTO updatedUsuario = usuarioService.updateUser(id, usuarioUpdateDTO);
        return ResponseEntity.ok(updatedUsuario);
    }

    // DELETE /api/usuarios/{id}
    // Admin pode deletar qualquer um.
    // Secretario pode deletar Cliente, Veterinario, ou outro Secretario.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') " +
            "or (hasRole('SECRETARIO') and @securityExpressions.canSecretaryModifyUser(#id))")
    public void deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUser(id);
    }

    // GET /api/usuarios/clientes
    @GetMapping("/clientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
    public ResponseEntity<List<UsuarioDTO>> getAllClientes() {
        List<UsuarioDTO> clientes = usuarioService.findAllClients();
        return ResponseEntity.ok(clientes);
    }

    // GET /api/usuarios/veterinarios
    @GetMapping("/veterinarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
    public ResponseEntity<List<UsuarioDTO>> getAllVeterinarios() {
        List<UsuarioDTO> veterinarios = usuarioService.findAllVeterinarios();
        return ResponseEntity.ok(veterinarios);
    }

    // NOVO: Adiciona um ExceptionHandler para ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // Retorna 404 Not Found
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}