package com.clinicaveterinaria.clinicaveterinaria.repository;

import com.clinicaveterinaria.clinicaveterinaria.model.entity.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByVeterinarioId(Long veterinarioId);
    // CORRIGIDO: findByDonoId -> findByClienteId
    List<Consulta> findByClienteId(Long clienteId); // Para o cliente ver suas consultas
    boolean existsByVeterinarioIdAndDataHora(Long veterinarioId, LocalDateTime dataHora);
}