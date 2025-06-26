package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.ClienteDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.PetDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.SecretarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.TipoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.VeterinarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.AuthService; // Para registrar usuários
import com.clinicaveterinaria.clinicaveterinaria.service.SecretarioService;
import com.clinicaveterinaria.clinicaveterinaria.service.TipoVacinaService; // Para gerenciar tipos de vacina
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/secretarios")
@PreAuthorize("hasRole('SECRETARIO')") // Todos os métodos aqui exigem SECRETARIO
public class SecretarioController {

    @Autowired
    private SecretarioService secretarioService;
    @Autowired
    private AuthService authService; // Para registro de novos usuários
    @Autowired
    private TipoVacinaService tipoVacinaService; // Para gerenciar tipos de vacina

    // Gerenciar Consultas
    @PostMapping("/consultas")
    public ResponseEntity<ConsultaDTO> agendarConsulta(@RequestBody @Valid ConsultaDTO consultaDTO) {
        ConsultaDTO novaConsulta = secretarioService.agendarConsulta(consultaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaConsulta);
    }

    @GetMapping("/consultas/medico/{veterinarioId}")
    public ResponseEntity<List<ConsultaDTO>> getConsultasByMedico(@PathVariable Long veterinarioId) {
        List<ConsultaDTO> consultas = secretarioService.findConsultasByMedico(veterinarioId);
        return ResponseEntity.ok(consultas);
    }

    // Gerenciar Veterinários
    @PostMapping("/veterinarios")
    public ResponseEntity<VeterinarioDTO> cadastrarVeterinario(@RequestBody @Valid VeterinarioDTO veterinarioDTO) {
        VeterinarioDTO novoVeterinario = authService.registerVeterinario(veterinarioDTO); // Usa authService para registrar
        return ResponseEntity.status(HttpStatus.CREATED).body(novoVeterinario);
    }

    @GetMapping("/veterinarios")
    public ResponseEntity<List<VeterinarioDTO>> getAllVeterinarios() {
        // Implementar no SecretarioService ou VeterinarioService um método para listar todos os veterinários
        // Exemplo: return ResponseEntity.ok(secretarioService.findAllVeterinarios());
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // Gerenciar Tipos de Vacina
    @PostMapping("/vacinas/tipos")
    public ResponseEntity<TipoVacinaDTO> cadastrarTipoVacina(@RequestBody @Valid TipoVacinaDTO tipoVacinaDTO) {
        TipoVacinaDTO novoTipo = tipoVacinaService.createTipoVacina(tipoVacinaDTO); // Usa TipoVacinaService
        return ResponseEntity.status(HttpStatus.CREATED).body(novoTipo);
    }

    @GetMapping("/vacinas/tipos")
    public ResponseEntity<List<TipoVacinaDTO>> getAllTiposVacina() {
        return ResponseEntity.ok(tipoVacinaService.findAllTipoVacinas());
    }

    // Gerenciar Pets
    @PostMapping("/pets")
    public ResponseEntity<PetDTO> cadastrarPet(@RequestBody @Valid PetDTO petDTO) {
        PetDTO novoPet = secretarioService.cadastrarPet(petDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPet);
    }

    // Gerenciar Clientes (registro, pois ClienteDTO herda UsuarioRegisterDTO)
    @PostMapping("/clientes")
    public ResponseEntity<ClienteDTO> cadastrarCliente(@RequestBody @Valid ClienteDTO clienteDTO) {
        ClienteDTO novoCliente = secretarioService.cadastrarCliente(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCliente);
    }
}


