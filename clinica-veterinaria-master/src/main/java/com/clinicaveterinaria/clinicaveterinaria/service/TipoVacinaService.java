package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.TipoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.TipoVacina;
import com.clinicaveterinaria.clinicaveterinaria.repository.TipoVacinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TipoVacinaService {

    @Autowired
    private TipoVacinaRepository tipoVacinaRepository;

    @Transactional(readOnly = true)
    public List<TipoVacinaDTO> findAllTipoVacinas() {
        return tipoVacinaRepository.findAll().stream()
                .map(this::convertToTipoVacinaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TipoVacinaDTO findTipoVacinaById(Long id) {
        TipoVacina tipoVacina = tipoVacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoVacina", "ID", id));
        return convertToTipoVacinaDTO(tipoVacina);
    }

    @Transactional
    public TipoVacinaDTO createTipoVacina(TipoVacinaDTO tipoVacinaDTO) {
        if (tipoVacinaRepository.findByNome(tipoVacinaDTO.getNome()).isPresent()) {
            throw new RuntimeException("Tipo de vacina com este nome já existe.");
        }
        TipoVacina tipoVacina = convertToTipoVacinaEntity(tipoVacinaDTO);
        TipoVacina savedTipo = tipoVacinaRepository.save(tipoVacina);
        return convertToTipoVacinaDTO(savedTipo);
    }

    @Transactional
    public TipoVacinaDTO updateTipoVacina(Long id, TipoVacinaDTO tipoVacinaDTO) {
        TipoVacina existingTipo = tipoVacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoVacina", "ID", id));

        // Verifica se o nome mudou e se o novo nome já existe
        if (!existingTipo.getNome().equals(tipoVacinaDTO.getNome()) &&
                tipoVacinaRepository.findByNome(tipoVacinaDTO.getNome()).isPresent()) {
            throw new RuntimeException("Já existe um tipo de vacina com este nome.");
        }

        existingTipo.setNome(tipoVacinaDTO.getNome());
        existingTipo.setDescricao(tipoVacinaDTO.getDescricao());
        existingTipo.setPeriodoReforcoEmMeses(tipoVacinaDTO.getPeriodoReforcoEmMeses());

        TipoVacina updatedTipo = tipoVacinaRepository.save(existingTipo);
        return convertToTipoVacinaDTO(updatedTipo);
    }

    @Transactional
    public void deleteTipoVacina(Long id) {
        if (!tipoVacinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("TipoVacina", "ID", id);
        }
        tipoVacinaRepository.deleteById(id);
    }

    // Métodos de Conversão (repetidos)
    private TipoVacinaDTO convertToTipoVacinaDTO(TipoVacina tipoVacina) {
        TipoVacinaDTO dto = new TipoVacinaDTO();
        dto.setId(tipoVacina.getId());
        dto.setNome(tipoVacina.getNome());
        dto.setDescricao(tipoVacina.getDescricao());
        dto.setPeriodoReforcoEmMeses(tipoVacina.getPeriodoReforcoEmMeses());
        return dto;
    }

    private TipoVacina convertToTipoVacinaEntity(TipoVacinaDTO dto) {
        TipoVacina tipoVacina = new TipoVacina();
        tipoVacina.setId(dto.getId());
        tipoVacina.setNome(dto.getNome());
        tipoVacina.setDescricao(dto.getDescricao());
        tipoVacina.setPeriodoReforcoEmMeses(dto.getPeriodoReforcoEmMeses());
        return tipoVacina;
    }
}
