package com.clinicaveterinaria.clinicaveterinaria.controller;

import com.clinicaveterinaria.clinicaveterinaria.model.dto.PetDTO;
import com.clinicaveterinaria.clinicaveterinaria.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets") // <-- Caminho base para todos os endpoints de Pet
public class PetController {

    @Autowired
    private PetService petService;

    @PostMapping // Para criar um novo Pet
    // Permite ADMIN, SECRETARIO ou o próprio CLIENTE logado (que será o dono)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO', 'CLIENTE')")
    public ResponseEntity<PetDTO> createPet(@RequestBody @Valid PetDTO petDTO) {
        PetDTO newPet = petService.savePet(petDTO); // Chamada ao método savePet do PetService
        return ResponseEntity.status(HttpStatus.CREATED).body(newPet);
    }

    @GetMapping // Para listar todos os Pets (geralmente só para Admin/Secretario)
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO')")
    public ResponseEntity<List<PetDTO>> getAllPets() {
        return ResponseEntity.ok(petService.findAllPets());
    }

    @GetMapping("/{id}") // Para buscar um Pet por ID
    // Permite ADMIN, SECRETARIO, ou o CLIENTE se for o dono do pet
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO') or (hasRole('CLIENTE') and @petSecurityExpressions.isOwner(#id))")
    public ResponseEntity<PetDTO> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.findPetById(id)); // Chamada ao método findPetById do PetService
    }

    @PutMapping("/{id}") // Para atualizar um Pet
    // Permite ADMIN, SECRETARIO, ou o CLIENTE se for o dono do pet
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO') or (hasRole('CLIENTE') and @petSecurityExpressions.isOwner(#id))")
    public ResponseEntity<PetDTO> updatePet(@PathVariable Long id, @RequestBody @Valid PetDTO petDTO) {
        PetDTO updatedPet = petService.updatePet(id, petDTO);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{id}") // Para deletar um Pet
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Permite ADMIN, SECRETARIO, ou o CLIENTE se for o dono do pet
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIO') or (hasRole('CLIENTE') and @petSecurityExpressions.isOwner(#id))")
    public void deletePet(@PathVariable Long id) {
        petService.deletePet(id);
    }
}