package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.PetDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@PreAuthorize("hasRole('CLIENTE')") // Garante que apenas CLIENTES acessem este controller
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/meus-pets")
    public ResponseEntity<List<PetDTO>> getMeusPets(@AuthenticationPrincipal Usuario currentUser) {
        List<PetDTO> pets = clienteService.findPetsByClienteEmail(currentUser.getEmail());
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/minhas-consultas")
    public ResponseEntity<List<ConsultaDTO>> getMinhasConsultas(@AuthenticationPrincipal Usuario currentUser) {
        List<ConsultaDTO> consultas = clienteService.findConsultasByClienteEmail(currentUser.getEmail());
        return ResponseEntity.ok(consultas);
    }

    @PostMapping("/pets")
    public ResponseEntity<PetDTO> createPet(@RequestBody @Valid PetDTO petDTO, @AuthenticationPrincipal Usuario currentUser) {
        // Validação: garante que o pet está sendo criado para o cliente logado
        if (!petDTO.getDonoId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Ou throw uma exceção
        }
        PetDTO newPet = clienteService.createPetForLoggedClient(petDTO, currentUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(newPet);
    }

    @PostMapping("/consultas")
    public ResponseEntity<ConsultaDTO> agendarConsulta(@RequestBody @Valid ConsultaDTO consultaDTO, @AuthenticationPrincipal Usuario currentUser) {
        // Validação: garante que a consulta está sendo agendada para o cliente logado
        if (!consultaDTO.getClienteId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Ou throw uma exceção
        }
        ConsultaDTO novaConsulta = clienteService.agendarConsultaForLoggedClient(consultaDTO, currentUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(novaConsulta);
    }
}
