package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.*;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.*;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.StatusConsulta;
import com.clinicaveterinaria.clinicaveterinaria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecretarioService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private VeterinarioRepository veterinarioRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private ConsultaRepository consultaRepository;
    @Autowired
    private TipoVacinaRepository tipoVacinaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // Para cadastrar novos usuários

    @Transactional
    public ConsultaDTO agendarConsulta(ConsultaDTO consultaDTO) {
        Cliente cliente = clienteRepository.findById(consultaDTO.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "ID", consultaDTO.getClienteId()));
        Pet pet = petRepository.findById(consultaDTO.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", "ID", consultaDTO.getPetId()));
        Veterinario veterinario = veterinarioRepository.findById(consultaDTO.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "ID", consultaDTO.getVeterinarioId()));

        // CORRIGIDO: existsByVeterinarioAndDataHora -> existsByVeterinarioIdAndDataHora
        if (consultaRepository.existsByVeterinarioIdAndDataHora(veterinario.getId(), consultaDTO.getDataHora())) {
            throw new RuntimeException("Veterinário já tem consulta agendada para este horário.");
        }

        Consulta consulta = convertToConsultaEntity(consultaDTO);
        consulta.setCliente(cliente);
        consulta.setPet(pet);
        consulta.setVeterinario(veterinario);
        consulta.setStatus(StatusConsulta.AGENDADA);

        Consulta savedConsulta = consultaRepository.save(consulta);
        return convertToConsultaDTO(savedConsulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaDTO> findConsultasByMedico(Long veterinarioId) {
        Veterinario veterinario = veterinarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinário", "ID", veterinarioId));
        // CORRIGIDO: findByVeterinario -> findByVeterinarioId
        return consultaRepository.findByVeterinarioId(veterinario.getId()).stream()
                .map(this::convertToConsultaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VeterinarioDTO cadastrarVeterinario(VeterinarioDTO veterinarioDTO) {
        if (veterinarioRepository.findByEmail(veterinarioDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email de veterinário já cadastrado.");
        }
        if (veterinarioRepository.findByCrmv(veterinarioDTO.getCrmv()).isPresent()) {
            throw new RuntimeException("CRMV de veterinário já cadastrado.");
        }

        String encryptedPassword = passwordEncoder.encode(veterinarioDTO.getSenha());
        // Chamando o construtor correto de Veterinario (nomeCompleto é o parâmetro 'nome' da superclasse)
        Veterinario newVeterinario = new Veterinario(
                veterinarioDTO.getEmail(),
                encryptedPassword,
                veterinarioDTO.getNomeCompleto(), // Passando nomeCompleto para o construtor da entidade filha
                veterinarioDTO.getCrmv(),
                veterinarioDTO.getEspecialidade()
        );
        Veterinario savedVeterinario = veterinarioRepository.save(newVeterinario);
        veterinarioDTO.setId(savedVeterinario.getId());
        veterinarioDTO.setSenha(null); // Não retornar a senha
        return veterinarioDTO;
    }

    @Transactional
    public TipoVacinaDTO cadastrarTipoVacina(TipoVacinaDTO tipoVacinaDTO) {
        if (tipoVacinaRepository.findByNome(tipoVacinaDTO.getNome()).isPresent()) {
            throw new RuntimeException("Tipo de vacina com este nome já existe.");
        }
        TipoVacina tipoVacina = convertToTipoVacinaEntity(tipoVacinaDTO);
        TipoVacina savedTipo = tipoVacinaRepository.save(tipoVacina);
        return convertToTipoVacinaDTO(savedTipo);
    }

    @Transactional
    public PetDTO cadastrarPet(PetDTO petDTO) {
        Cliente dono = clienteRepository.findById(petDTO.getDonoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente (Dono do Pet)", "ID", petDTO.getDonoId()));
        Pet pet = convertToPetEntity(petDTO);
        pet.setDono(dono);
        Pet savedPet = petRepository.save(pet);
        return convertToPetDTO(savedPet);
    }

    @Transactional
    public ClienteDTO cadastrarCliente(ClienteDTO clienteDTO) {
        if (clienteRepository.findByEmail(clienteDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email de cliente já cadastrado.");
        }
        if (clienteRepository.findByCpf(clienteDTO.getCpf()).isPresent()) {
            throw new RuntimeException("CPF de cliente já cadastrado.");
        }
        String encryptedPassword = passwordEncoder.encode(clienteDTO.getSenha());
        // Chamando o construtor correto de Cliente (nomeCompleto é o parâmetro 'nome' da superclasse)
        Cliente newCliente = new Cliente(
                clienteDTO.getEmail(),
                encryptedPassword,
                clienteDTO.getNomeCompleto(), // Passando nomeCompleto para o construtor da entidade filha
                clienteDTO.getTelefone(),
                clienteDTO.getCpf()
        );
        Cliente savedCliente = clienteRepository.save(newCliente);
        clienteDTO.setId(savedCliente.getId());
        clienteDTO.setSenha(null);
        return clienteDTO;
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
        consulta.setId(dto.getId());
        consulta.setDataHora(dto.getDataHora());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamento(dto.getTratamento());
        consulta.setObservacoes(dto.getObservacoes());
        return consulta;
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
        // Aqui, o DTO do Pet pode ter um campo para o nome do dono, se você quiser exibi-lo
        // dto.setDonoNome(pet.getDono().getNome()); // Exemplo: se PetDTO tiver setDonoNome
        return dto;
    }

    private Pet convertToPetEntity(PetDTO dto) {
        Pet pet = new Pet();
        pet.setId(dto.getId());
        pet.setNome(dto.getNome());
        pet.setEspecie(dto.getEspecie());
        pet.setRaca(dto.getRaca());
        pet.setDataNascimento(dto.getDataNascimento());
        pet.setCor(dto.getCor());
        pet.setObservacoes(dto.getObservacoes());
        return pet;
    }

    private ClienteDTO convertToClienteDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        // CORRIGIDO: getNomeCompleto() -> getNome() em entidades
        dto.setNomeCompleto(cliente.getNome());
        dto.setEmail(cliente.getEmail());
        dto.setTelefone(cliente.getTelefone());
        dto.setCpf(cliente.getCpf());
        // Senha não é exposta
        return dto;
    }
}