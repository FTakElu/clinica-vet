package com.clinicaveterinaria.clinicaveterinaria.repository;

import com.clinicaveterinaria.clinicaveterinaria.model.entity.TipoVacina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoVacinaRepository extends JpaRepository<TipoVacina, Long> {
    Optional<TipoVacina> findByNome(String nome);
}
