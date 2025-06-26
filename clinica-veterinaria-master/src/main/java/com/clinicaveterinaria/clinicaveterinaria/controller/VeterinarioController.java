package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.AplicacaoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.service.VeterinarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veterinarios")
@PreAuthorize("hasRole('VETERINARIO')") // Todos os métodos aqui exigem VETERINARIO
public class VeterinarioController {

    @Autowired
    private VeterinarioService veterinarioService;

    @GetMapping("/minhas-consultas")
    public ResponseEntity<List<ConsultaDTO>> getMinhasConsultas(@AuthenticationPrincipal Usuario currentUser) {
        List<ConsultaDTO> consultas = veterinarioService.findMinhasConsultas(currentUser.getEmail());
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/consultas/{id}/relatorio")
    public ResponseEntity<String> gerarRelatorioConsulta(@PathVariable Long id, @AuthenticationPrincipal Usuario currentUser) {
        String relatorio = veterinarioService.gerarRelatorioConsulta(id, currentUser.getEmail());
        return ResponseEntity.ok(relatorio);
    }

    // Para atualizar o status da consulta para FINALIZADA e adicionar diagnóstico/tratamento
    @PutMapping("/consultas/{id}/finalizar")
    public ResponseEntity<ConsultaDTO> finalizarConsulta(
            @PathVariable Long id,
            @RequestBody ConsultaDTO consultaUpdateDTO, // Pode ter campos para diagnóstico/tratamento
            @AuthenticationPrincipal Usuario currentUser) {
        // Você pode passar o DTO completo para o serviço, e o serviço lida com a atualização dos campos relevantes
        // e a validação do veterinário logado.
        ConsultaDTO consultaFinalizada = veterinarioService.finalizarConsulta(id, currentUser.getEmail());
        return ResponseEntity.ok(consultaFinalizada);
    }

    @PostMapping("/vacinas/aplicacao")
    public ResponseEntity<AplicacaoVacinaDTO> registrarAplicacaoVacina(@RequestBody @Valid AplicacaoVacinaDTO aplicacaoVacinaDTO, @AuthenticationPrincipal Usuario currentUser) {
        AplicacaoVacinaDTO novaAplicacao = veterinarioService.registrarAplicacaoVacina(aplicacaoVacinaDTO, currentUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(novaAplicacao);
    }
}
