package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ClienteDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ConsultaDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.PetDTO;
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
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Transactional(readOnly = true)
    public List<PetDTO> findPetsByClienteEmail(String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "email", emailCliente));
        return cliente.getPets().stream()
                .map(this::convertToPetDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> findConsultasByClienteEmail(String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "email", emailCliente));
        return cliente.getConsultas().stream()
                .map(this::convertToConsultaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PetDTO createPetForLoggedClient(PetDTO petDTO, String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "email", emailCliente));

        Pet pet = convertToPetEntity(petDTO);
        pet.setDono(cliente);
        Pet savedPet = petRepository.save(pet);
        return convertToPetDTO(savedPet);
    }

    @Transactional
    public ConsultaDTO agendarConsultaForLoggedClient(ConsultaDTO consultaDTO, String emailCliente) {
        Cliente cliente = clienteRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "email", emailCliente));
        Pet pet = petRepository.findById(consultaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", consultaDTO.getPetId()));
        Veterinario veterinario = veterinarioRepository.findById(consultaDTO.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "ID", consultaDTO.getVeterinarioId()));

        if (!pet.getDono().getId().equals(cliente.getId())) {
            throw new RuntimeException("O pet selecionado não pertence ao cliente logado."); // Exceção de negócio
        }

        // CORRIGIDO: existsByVeterinarioAndDataHora -> existsByVeterinarioIdAndDataHora
        if (consultaRepository.existsByVeterinarioIdAndDataHora(veterinario.getId(), consultaDTO.getDataHora())) {
            throw new RuntimeException("Veterinário já tem consulta agendada para este horário.");
        }

        Consulta consulta = convertToConsultaEntity(consultaDTO);
        consulta.setCliente(cliente);
        consulta.setPet(pet);
        consulta.setVeterinario(veterinario);
        consulta.setStatus(StatusConsulta.AGENDADA); // Status inicial

        Consulta savedConsulta = consultaRepository.save(consulta);
        return convertToConsultaDTO(savedConsulta);
    }

    // Métodos de Conversão (poderiam estar em uma classe Mapper separada)
    private PetDTO convertToPetDTO(Pet pet) {
        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setNome(pet.getNome());
        dto.setEspecie(pet.getEspecie());
        dto.setRaca(pet.getRaca());
        dto.setDataNascimento(pet.getDataNascimento());
        dto.setCor(pet.getCor());
        dto.setObservacoes(pet.getObservacoes());
        dto.setDonoId(pet.getDono().getId());
        return dto;
    }

    private Pet convertToPetEntity(PetDTO dto) {
        Pet pet = new Pet();
        pet.setId(dto.getId()); // Pode ser null para nova entidade
        pet.setNome(dto.getNome());
        pet.setEspecie(dto.getEspecie());
        pet.setRaca(dto.getRaca());
        pet.setDataNascimento(dto.getDataNascimento());
        pet.setCor(dto.getCor());
        pet.setObservacoes(dto.getObservacoes());
        // Dono é settado no serviço
        return pet;
    }

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

    public List<ClienteDTO> getAllClientes() {
        return clienteRepository.findAll().stream()
                .map(cliente -> {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setId(cliente.getId());
                    dto.setEmail(cliente.getEmail());
                    dto.setNomeCompleto(cliente.getNome()); // AQUI: Mudança de getNomeCompleto() para getNome()
                    dto.setTelefone(cliente.getTelefone());
                    dto.setCpf(cliente.getCpf());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    public ClienteDTO getClienteById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setEmail(cliente.getEmail());
        dto.setNomeCompleto(cliente.getNome()); // AQUI: Mudança de getNomeCompleto() para getNome()
        dto.setTelefone(cliente.getTelefone());
        dto.setCpf(cliente.getCpf());
        return dto;
    }

}