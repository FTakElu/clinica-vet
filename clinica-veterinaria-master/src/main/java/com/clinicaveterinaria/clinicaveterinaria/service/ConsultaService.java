package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Cliente;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Consulta;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Pet;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Veterinario;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.StatusConsulta;
import com.clinicaveterinaria.clinicaveterinaria.repository.ClienteRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.ConsultaRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.PetRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private PetRepository petRepository;

    @Transactional(readOnly = true)
    public List<ConsultaDTO> findAllConsultas() {
        return consultaRepository.findAll().stream()
                .map(this::convertToConsultaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultaDTO findConsultaById(Long id) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", "ID", id));
        return convertToConsultaDTO(consulta);
    }

    @Transactional
    public ConsultaDTO createConsulta(ConsultaDTO consultaDTO) {
        Cliente cliente = clienteRepository.findById(consultaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "ID", consultaDTO.getClienteId()));
        Veterinario veterinario = veterinarioRepository.findById(consultaDTO.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "ID", consultaDTO.getVeterinarioId()));
        Pet pet = petRepository.findById(consultaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", consultaDTO.getPetId()));

        // CORRIGIDO: existsByVeterinarioAndDataHora -> existsByVeterinarioIdAndDataHora
        if (consultaRepository.existsByVeterinarioIdAndDataHora(veterinario.getId(), consultaDTO.getDataHora())) {
            throw new RuntimeException("Veterinário já tem consulta agendada para este horário.");
        }

        Consulta consulta = convertToConsultaEntity(consultaDTO);
        consulta.setCliente(cliente);
        consulta.setVeterinario(veterinario);
        consulta.setPet(pet);
        consulta.setStatus(StatusConsulta.AGENDADA);

        Consulta savedConsulta = consultaRepository.save(consulta);
        return convertToConsultaDTO(savedConsulta);
    }

    @Transactional
    public ConsultaDTO updateConsulta(Long id, ConsultaDTO consultaDTO) {
        Consulta existingConsulta = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", "ID", id));

        Cliente cliente = clienteRepository.findById(consultaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "ID", consultaDTO.getClienteId()));
        Veterinario veterinario = veterinarioRepository.findById(consultaDTO.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "ID", consultaDTO.getVeterinarioId()));
        Pet pet = petRepository.findById(consultaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", consultaDTO.getPetId()));

        // Se a data/hora do agendamento foi alterada, verificar novamente a disponibilidade
        if (!existingConsulta.getDataHora().equals(consultaDTO.getDataHora()) || !existingConsulta.getVeterinario().getId().equals(veterinario.getId())) {
            if (consultaRepository.existsByVeterinarioIdAndDataHora(veterinario.getId(), consultaDTO.getDataHora())) {
                throw new RuntimeException("Veterinário já tem consulta agendada para este novo horário.");
            }
        }

        existingConsulta.setCliente(cliente);
        existingConsulta.setVeterinario(veterinario);
        existingConsulta.setPet(pet);
        existingConsulta.setDataHora(consultaDTO.getDataHora());
        existingConsulta.setStatus(consultaDTO.getStatus());
        existingConsulta.setDiagnostico(consultaDTO.getDiagnostico());
        existingConsulta.setTratamento(consultaDTO.getTratamento());
        existingConsulta.setObservacoes(consultaDTO.getObservacoes());

        Consulta updatedConsulta = consultaRepository.save(existingConsulta);
        return convertToConsultaDTO(updatedConsulta);
    }

    @Transactional
    public void deleteConsulta(Long id) {
        if (!consultaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consulta", "ID", id);
        }
        consultaRepository.deleteById(id);
    }

    // Métodos de Conversão
    private ConsultaDTO convertToConsultaDTO(Consulta consulta) {
        ConsultaDTO dto = new ConsultaDTO();
        dto.setId(consulta.getId());
        dto.setClienteId(consulta.getCliente().getId());
        // CORRIGIDO: getNomeCompleto() -> getNome() em entidades
        dto.setClienteNome(consulta.getCliente().getNome());
        dto.setVeterinarioId(consulta.getVeterinario().getId());
        // CORRIGIDO: getNomeCompleto() -> getNome() em entidades
        dto.setVeterinarioNome(consulta.getVeterinario().getNome());
        dto.setPetId(consulta.getPet().getId());
        dto.setPetNome(consulta.getPet().getNome());
        dto.setDataHora(consulta.getDataHora());
        dto.setStatus(consulta.getStatus());
        dto.setDiagnostico(consulta.getDiagnostico());
        dto.setTratamento(consulta.getTratamento());
        dto.setObservacoes(consulta.getObservacoes());
        return dto;
    }

    private Consulta convertToConsultaEntity(ConsultaDTO dto) {
        Consulta consulta = new Consulta();
        consulta.setId(dto.getId()); // Pode ser null para nova entidade
        consulta.setDataHora(dto.getDataHora());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamento(dto.getTratamento());
        consulta.setObservacoes(dto.getObservacoes());
        // Cliente, Pet, Veterinário e Status são settados no serviço
        return consulta;
    }
}
