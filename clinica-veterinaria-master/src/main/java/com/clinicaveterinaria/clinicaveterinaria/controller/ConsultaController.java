package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas") // <-- MUDANÇA AQUI: Adicionado /api
// Este controller pode ser acessado por ADMIN e SECRETARIO, dependendo da granularidade que você quer
@PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @GetMapping
    public ResponseEntity<List<ConsultaDTO>> getAllConsultas() {
        return ResponseEntity.ok(consultaService.findAllConsultas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDTO> getConsultaById(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.findConsultaById(id));
    }

    @PostMapping
    public ResponseEntity<ConsultaDTO> createConsulta(@RequestBody @Valid ConsultaDTO consultaDTO) {
        ConsultaDTO novaConsulta = consultaService.createConsulta(consultaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaConsulta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultaDTO> updateConsulta(@PathVariable Long id, @RequestBody @Valid ConsultaDTO consultaDTO) {
        ConsultaDTO updatedConsulta = consultaService.updateConsulta(id, consultaDTO);
        return ResponseEntity.ok(updatedConsulta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsulta(@PathVariable Long id) {
        consultaService.deleteConsulta(id);
    }
}