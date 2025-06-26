package com.clinicaveterinaria.clinicaveterinaria.repository;

import com.clinicaveterinaria.clinicaveterinaria.model.entity.AplicacaoVacina;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AplicacaoVacinaRepository extends JpaRepository<AplicacaoVacina, Long> {
    List<AplicacaoVacina> findByPetId(Long petId);
    List<AplicacaoVacina> findByVeterinarioResponsavelId(Long veterinarioResponsavelId);
}