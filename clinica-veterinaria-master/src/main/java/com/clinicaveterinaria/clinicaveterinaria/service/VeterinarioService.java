package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.AplicacaoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.*;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.StatusConsulta;
import com.clinicaveterinaria.clinicaveterinaria.repository.AplicacaoVacinaRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.ConsultaRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.PetRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.TipoVacinaRepository;
import com.clinicaveterinaria.clinicaveterinaria.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private AplicacaoVacinaRepository aplicacaoVacinaRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private TipoVacinaRepository tipoVacinaRepository;

    @Transactional(readOnly = true)
    public List<ConsultaDTO> findMinhasConsultas(String emailVeterinario) {
        Veterinario veterinario = veterinarioRepository.findByEmail(emailVeterinario)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "email", emailVeterinario));
        // CORRIGIDO: findByVeterinario -> findByVeterinarioId
        return consultaRepository.findByVeterinarioId(veterinario.getId()).stream()
                .map(this::convertToConsultaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String gerarRelatorioConsulta(Long consultaId, String emailVeterinario) {
        Veterinario veterinario = veterinarioRepository.findByEmail(emailVeterinario)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "email", emailVeterinario));
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", "ID", consultaId));

        if (!consulta.getVeterinario().getId().equals(veterinario.getId())) {
            throw new RuntimeException("Veterinário não tem permissão para gerar relatório desta consulta.");
        }

        // Construir um relatório simples. Em um caso real, seria mais elaborado.
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("Relatório da Consulta ID: ").append(consulta.getId()).append("\n");
        relatorio.append("Data e Hora: ").append(consulta.getDataHora()).append("\n");
        // CORRIGIDO: getNomeCompleto() -> getNome() em entidades
        relatorio.append("Cliente: ").append(consulta.getCliente().getNome()).append("\n");
        relatorio.append("Pet: ").append(consulta.getPet().getNome()).append(" (").append(consulta.getPet().getEspecie()).append(")\n");
        // CORRIGIDO: getNomeCompleto() -> getNome() em entidades
        relatorio.append("Veterinário: ").append(consulta.getVeterinario().getNome()).append("\n");
        relatorio.append("Status: ").append(consulta.getStatus()).append("\n");
        relatorio.append("Diagnóstico: ").append(consulta.getDiagnostico() != null ? consulta.getDiagnostico() : "Não informado").append("\n");
        relatorio.append("Tratamento: ").append(consulta.getTratamento() != null ? consulta.getTratamento() : "Não informado").append("\n");
        relatorio.append("Observações: ").append(consulta.getObservacoes() != null ? consulta.getObservacoes() : "Nenhuma").append("\n");

        return relatorio.toString();
    }

    @Transactional
    public ConsultaDTO finalizarConsulta(Long consultaId, String emailVeterinario) {
        Veterinario veterinario = veterinarioRepository.findByEmail(emailVeterinario)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "email", emailVeterinario));
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", "ID", consultaId));

        if (!consulta.getVeterinario().getId().equals(veterinario.getId())) {
            throw new RuntimeException("Veterinário não tem permissão para finalizar esta consulta.");
        }
        if (consulta.getStatus().equals(StatusConsulta.FINALIZADA) || consulta.getStatus().equals(StatusConsulta.CANCELADA)) {
            throw new RuntimeException("Consulta já foi finalizada ou cancelada.");
        }

        consulta.setStatus(StatusConsulta.FINALIZADA);
        // Atualize diagnóstico, tratamento, observações se necessário com um DTO de atualização
        Consulta updatedConsulta = consultaRepository.save(consulta);
        return convertToConsultaDTO(updatedConsulta);
    }

    @Transactional
    public AplicacaoVacinaDTO registrarAplicacaoVacina(AplicacaoVacinaDTO aplicacaoVacinaDTO, String emailVeterinario) {
        Veterinario veterinario = veterinarioRepository.findByEmail(emailVeterinario)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "email", emailVeterinario));
        Pet pet = petRepository.findById(aplicacaoVacinaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", aplicacaoVacinaDTO.getPetId()));
        TipoVacina tipoVacina = tipoVacinaRepository.findById(aplicacaoVacinaDTO.getTipoVacinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Vacina", "ID", aplicacaoVacinaDTO.getTipoVacinaId()));

        AplicacaoVacina aplicacaoVacina = convertToAplicacaoVacinaEntity(aplicacaoVacinaDTO);
        aplicacaoVacina.setPet(pet);
        aplicacaoVacina.setTipoVacina(tipoVacina);
        aplicacaoVacina.setVeterinarioResponsavel(veterinario); // O veterinário logado é o responsável

        // Calcula a data da próxima aplicação
        if (tipoVacina.getPeriodoReforcoEmMeses() > 0) {
            aplicacaoVacina.setDataProximaAplicacao(aplicacaoVacina.getDataAplicacao().plusMonths(tipoVacina.getPeriodoReforcoEmMeses()));
        }

        AplicacaoVacina savedAplicacao = aplicacaoVacinaRepository.save(aplicacaoVacina);
        return convertToAplicacaoVacinaDTO(savedAplicacao);
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

    private AplicacaoVacinaDTO convertToAplicacaoVacinaDTO(AplicacaoVacina aplicacaoVacina) {
        AplicacaoVacinaDTO dto = new AplicacaoVacinaDTO();
        dto.setId(aplicacaoVacina.getId());
        dto.setPetId(aplicacaoVacina.getPet().getId());
        dto.setPetNome(aplicacaoVacina.getPet().getNome());
        dto.setTipoVacinaId(aplicacaoVacina.getTipoVacina().getId());
        dto.setTipoVacinaNome(aplicacaoVacina.getTipoVacina().getNome());
        if (aplicacaoVacina.getVeterinarioResponsavel() != null) {
            dto.setVeterinarioResponsavelId(aplicacaoVacina.getVeterinarioResponsavel().getId());
            // CORRIGIDO: getNomeCompleto() -> getNome() em entidades
            dto.setVeterinarioResponsavelNome(aplicacaoVacina.getVeterinarioResponsavel().getNome());
        }
        dto.setDataAplicacao(aplicacaoVacina.getDataAplicacao());
        dto.setDataProximaAplicacao(aplicacaoVacina.getDataProximaAplicacao());
        dto.setLoteVacina(aplicacaoVacina.getLoteVacina());
        dto.setObservacoes(aplicacaoVacina.getObservacoes());
        return dto;
    }

    private AplicacaoVacina convertToAplicacaoVacinaEntity(AplicacaoVacinaDTO dto) {
        AplicacaoVacina aplicacaoVacina = new AplicacaoVacina();
        aplicacaoVacina.setId(dto.getId());
        aplicacaoVacina.setDataAplicacao(dto.getDataAplicacao());
        aplicacaoVacina.setLoteVacina(dto.getLoteVacina());
        aplicacaoVacina.setObservacoes(dto.getObservacoes());
        // Pet, TipoVacina e VeterinarioResponsavel são settados no serviço
        return aplicacaoVacina;
    }
}
