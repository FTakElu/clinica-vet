package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.AplicacaoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.VacinaService; // Renomeado para AplicacaoVacinaService
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vacinas/aplicacoes") // Endpoint mais específico para aplicações de vacina
@PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO', 'VETERINARIO')") // Veterinário também pode ver e registrar as suas
public class VacinaController { // Renomeado para AplicacaoVacinaController

    @Autowired
    private VacinaService vacinaService; // Injetando o serviço de Aplicação de Vacina

    @GetMapping
    public ResponseEntity<List<AplicacaoVacinaDTO>> getAllAplicacoesVacina() {
        return ResponseEntity.ok(vacinaService.findAllAplicacoesVacina());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AplicacaoVacinaDTO> getAplicacaoVacinaById(@PathVariable Long id) {
        return ResponseEntity.ok(vacinaService.findAplicacaoVacinaById(id));
    }

    // Secretário pode criar uma aplicação de vacina
    @PostMapping
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<AplicacaoVacinaDTO> createAplicacaoVacina(@RequestBody @Valid AplicacaoVacinaDTO aplicacaoVacinaDTO) {
        AplicacaoVacinaDTO novaAplicacao = vacinaService.createAplicacaoVacina(aplicacaoVacinaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaAplicacao);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO', 'VETERINARIO')") // Pode ser editada por quem registrou ou admin/secretario
    public ResponseEntity<AplicacaoVacinaDTO> updateAplicacaoVacina(@PathVariable Long id, @RequestBody @Valid AplicacaoVacinaDTO aplicacaoVacinaDTO) {
        AplicacaoVacinaDTO updatedAplicacao = vacinaService.updateAplicacaoVacina(id, aplicacaoVacinaDTO);
        return ResponseEntity.ok(updatedAplicacao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')") // Somente admin/secretario podem deletar
    public void deleteAplicacaoVacina(@PathVariable Long id) {
        vacinaService.deleteAplicacaoVacina(id);
    }
}
