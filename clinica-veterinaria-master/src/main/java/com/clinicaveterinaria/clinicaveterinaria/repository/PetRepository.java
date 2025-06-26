package com.clinicaveterinaria.clinicaveterinaria.repository;

import com.clinicaveterinaria.clinicaveterinaria.model.entity.Cliente;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByDono(Cliente dono);
    //List<Pet> findByDonoId(Long donoId);
}
