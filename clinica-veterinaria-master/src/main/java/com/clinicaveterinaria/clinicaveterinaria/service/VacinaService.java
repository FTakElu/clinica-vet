package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.AplicacaoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.TipoVacinaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.AplicacaoVacina;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Pet;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.TipoVacina;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Veterinario;
import com.clinicaveterinaria.clinicaveterinaria.repository.AplicacaoVacinaRepository;
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
public class VacinaService { // Nome da classe mantém VacinaService para corresponder ao controller

    @Autowired
    private AplicacaoVacinaRepository aplicacaoVacinaRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private TipoVacinaRepository tipoVacinaRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;

    // Métodos CRUD para AplicacaoVacina (chamados pelo VacinaController)

    @Transactional(readOnly = true)
    public List<AplicacaoVacinaDTO> findAllAplicacoesVacina() {
        return aplicacaoVacinaRepository.findAll().stream()
                .map(this::convertToAplicacaoVacinaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AplicacaoVacinaDTO findAplicacaoVacinaById(Long id) {
        AplicacaoVacina aplicacaoVacina = aplicacaoVacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicação de Vacina", "ID", id));
        return convertToAplicacaoVacinaDTO(aplicacaoVacina);
    }

    @Transactional
    public AplicacaoVacinaDTO createAplicacaoVacina(AplicacaoVacinaDTO aplicacaoVacinaDTO) {
        Pet pet = petRepository.findById(aplicacaoVacinaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", aplicacaoVacinaDTO.getPetId()));
        TipoVacina tipoVacina = tipoVacinaRepository.findById(aplicacaoVacinaDTO.getTipoVacinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Vacina", "ID", aplicacaoVacinaDTO.getTipoVacinaId()));
        Veterinario veterinarioResponsavel = veterinarioRepository.findById(aplicacaoVacinaDTO.getVeterinarioResponsavelId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário Responsável", "ID", aplicacaoVacinaDTO.getVeterinarioResponsavelId()));

        AplicacaoVacina aplicacaoVacina = convertToAplicacaoVacinaEntity(aplicacaoVacinaDTO);
        aplicacaoVacina.setPet(pet);
        aplicacaoVacina.setTipoVacina(tipoVacina);
        aplicacaoVacina.setVeterinarioResponsavel(veterinarioResponsavel);

        // Calcula a data da próxima aplicação
        // CORRIGIDO: Removido '!= null' para tipo primitivo (int/long)
        if (tipoVacina.getPeriodoReforcoEmMeses() > 0) {
            aplicacaoVacina.setDataProximaAplicacao(aplicacaoVacina.getDataAplicacao().plusMonths(tipoVacina.getPeriodoReforcoEmMeses()));
        } else {
            aplicacaoVacina.setDataProximaAplicacao(null); // Ou alguma lógica padrão se não houver reforço
        }


        AplicacaoVacina savedAplicacao = aplicacaoVacinaRepository.save(aplicacaoVacina);
        return convertToAplicacaoVacinaDTO(savedAplicacao);
    }

    @Transactional
    public AplicacaoVacinaDTO updateAplicacaoVacina(Long id, AplicacaoVacinaDTO aplicacaoVacinaDTO) {
        AplicacaoVacina existingAplicacao = aplicacaoVacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicação de Vacina", "ID", id));

        Pet pet = petRepository.findById(aplicacaoVacinaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", aplicacaoVacinaDTO.getPetId()));
        TipoVacina tipoVacina = tipoVacinaRepository.findById(aplicacaoVacinaDTO.getTipoVacinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Vacina", "ID", aplicacaoVacinaDTO.getTipoVacinaId()));
        Veterinario veterinarioResponsavel = veterinarioRepository.findById(aplicacaoVacinaDTO.getVeterinarioResponsavelId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário Responsável", "ID", aplicacaoVacinaDTO.getVeterinarioResponsavelId()));

        existingAplicacao.setPet(pet);
        existingAplicacao.setTipoVacina(tipoVacina);
        existingAplicacao.setVeterinarioResponsavel(veterinarioResponsavel);
        existingAplicacao.setDataAplicacao(aplicacaoVacinaDTO.getDataAplicacao());
        existingAplicacao.setLoteVacina(aplicacaoVacinaDTO.getLoteVacina());
        existingAplicacao.setObservacoes(aplicacaoVacinaDTO.getObservacoes());

        // Recalcular a data da próxima aplicação se o tipo de vacina mudou ou a data de aplicação
        // CORRIGIDO: Removido '!= null' para tipo primitivo (int/long)
        if (tipoVacina.getPeriodoReforcoEmMeses() > 0) {
            existingAplicacao.setDataProximaAplicacao(existingAplicacao.getDataAplicacao().plusMonths(tipoVacina.getPeriodoReforcoEmMeses()));
        } else {
            existingAplicacao.setDataProximaAplicacao(null);
        }

        AplicacaoVacina updatedAplicacao = aplicacaoVacinaRepository.save(existingAplicacao);
        return convertToAplicacaoVacinaDTO(updatedAplicacao);
    }

    @Transactional
    public void deleteAplicacaoVacina(Long id) {
        if (!aplicacaoVacinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aplicação de Vacina", "ID", id);
        }
        aplicacaoVacinaRepository.deleteById(id);
    }

    // Método que você já tinha para registrar aplicação (mantido, mas `createAplicacaoVacina` acima é mais genérico)
    @Transactional
    public AplicacaoVacinaDTO registrarAplicacaoVacina(AplicacaoVacinaDTO aplicacaoVacinaDTO, String emailVeterinario) {
        Veterinario veterinario = veterinarioRepository.findByEmail(emailVeterinario)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "email", emailVeterinario));
        // Redireciona para o método create com o ID do veterinário logado
        aplicacaoVacinaDTO.setVeterinarioResponsavelId(veterinario.getId());
        return createAplicacaoVacina(aplicacaoVacinaDTO);
    }


    @Transactional(readOnly = true)
    public List<AplicacaoVacinaDTO> findAplicacoesByPetId(Long petId) {
        petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", petId));
        return aplicacaoVacinaRepository.findByPetId(petId).stream()
                .map(this::convertToAplicacaoVacinaDTO)
                .collect(Collectors.toList());
    }

    // Métodos de Conversão
    private AplicacaoVacinaDTO convertToAplicacaoVacinaDTO(AplicacaoVacina aplicacaoVacina) {
        AplicacaoVacinaDTO dto = new AplicacaoVacinaDTO();
        dto.setId(aplicacaoVacina.getId());
        dto.setPetId(aplicacaoVacina.getPet().getId());
        dto.setPetNome(aplicacaoVacina.getPet().getNome());
        dto.setTipoVacinaId(aplicacaoVacina.getTipoVacina().getId());
        dto.setTipoVacinaNome(aplicacaoVacina.getTipoVacina().getNome());
        if (aplicacaoVacina.getVeterinarioResponsavel() != null) {
            dto.setVeterinarioResponsavelId(aplicacaoVacina.getVeterinarioResponsavel().getId());
            dto.setVeterinarioResponsavelNome(aplicacaoVacina.getVeterinarioResponsavel().getNome()); // Corrigido
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
        return aplicacaoVacina;
    }

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
