package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.AuthDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ClienteDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.TokenResponseDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioRegisterDTO; // Usando o nome correto
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.VeterinarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager; // IMPORTANTE: Importar AuthenticationManager
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private AuthenticationManager authenticationManager; // NOVO: Injetar AuthenticationManager aqui

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid AuthDTO authDTO) {
        // Passar o authenticationManager injetado para o AuthService.login
        TokenResponseDTO token = authService.login(authDTO, authenticationManager);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register/cliente")
    public ResponseEntity<ClienteDTO> registerCliente(@RequestBody @Valid ClienteDTO clienteDTO) {
        ClienteDTO newCliente = authService.registerCliente(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCliente);
    }

    @PostMapping("/register/user-with-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> registerUserWithRole(@RequestBody @Valid UsuarioRegisterDTO usuarioRegisterDTO) { // Usando UsuarioRegisterDTO
        UsuarioDTO newUser = authService.registerUserWithRole(usuarioRegisterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PostMapping("/register/veterinario")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
    public ResponseEntity<VeterinarioDTO> registerVeterinario(@RequestBody @Valid VeterinarioDTO veterinarioDTO) {
        VeterinarioDTO newVeterinario = authService.registerVeterinario(veterinarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newVeterinario);
    }
}
