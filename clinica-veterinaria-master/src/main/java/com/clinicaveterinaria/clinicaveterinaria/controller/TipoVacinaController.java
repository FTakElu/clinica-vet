package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.TipoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.TipoVacinaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-vacina")
@PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
public class TipoVacinaController {

    @Autowired
    private TipoVacinaService tipoVacinaService;

    @GetMapping
    public ResponseEntity<List<TipoVacinaDTO>> getAllTipoVacinas() {
        return ResponseEntity.ok(tipoVacinaService.findAllTipoVacinas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoVacinaDTO> getTipoVacinaById(@PathVariable Long id) {
        return ResponseEntity.ok(tipoVacinaService.findTipoVacinaById(id));
    }

    @PostMapping
    public ResponseEntity<TipoVacinaDTO> createTipoVacina(@RequestBody @Valid TipoVacinaDTO tipoVacinaDTO) {
        TipoVacinaDTO novoTipo = tipoVacinaService.createTipoVacina(tipoVacinaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoTipo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoVacinaDTO> updateTipoVacina(@PathVariable Long id, @RequestBody @Valid TipoVacinaDTO tipoVacinaDTO) {
        TipoVacinaDTO updatedTipo = tipoVacinaService.updateTipoVacina(id, tipoVacinaDTO);
        return ResponseEntity.ok(updatedTipo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTipoVacina(@PathVariable Long id) {
        tipoVacinaService.deleteTipoVacina(id);
    }
}
