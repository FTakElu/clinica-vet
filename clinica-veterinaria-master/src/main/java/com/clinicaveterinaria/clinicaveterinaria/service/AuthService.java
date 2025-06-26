package com.clinicaveterinaria.clinicaveterinaria.service;

import com.clinicaveterinaria.clinicaveterinaria.exception.ResourceNotFoundException;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.AuthDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.ClienteDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.TokenResponseDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioRegisterDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.UsuarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.dto.VeterinarioDTO;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Cliente;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Secretario;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.model.entity.Veterinario;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import com.clinicaveterinaria.clinicaveterinaria.repository.UsuarioRepository;
import com.clinicaveterinaria.clinicaveterinaria.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public TokenResponseDTO login(AuthDTO authDTO, AuthenticationManager authenticationManager) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getSenha());
        var auth = authenticationManager.authenticate(usernamePassword);

        Usuario usuario = (Usuario) auth.getPrincipal();
        String token = tokenService.generateToken(usuario);
        return new TokenResponseDTO(token, usuario.getRole().name(), usuario.getId(), usuario.getNome());
    }

    public ClienteDTO registerCliente(ClienteDTO clienteDTO) {
        if (this.usuarioRepository.findByEmail(clienteDTO.getEmail()).isPresent()) {
            // Em uma aplicação real, você pode lançar uma exceção mais específica (ex: EmailAlreadyExistsException)
            throw new RuntimeException("Email já cadastrado.");
        }
        String encryptedPassword = passwordEncoder.encode(clienteDTO.getSenha());
        Cliente newCliente = new Cliente(
                clienteDTO.getEmail(),
                encryptedPassword,
                clienteDTO.getNomeCompleto(),
                clienteDTO.getTelefone(),
                clienteDTO.getCpf()
        );
        Cliente savedCliente = usuarioRepository.save(newCliente);

        // Retorna o DTO com o ID gerado e a senha nula
        clienteDTO.setId(savedCliente.getId());
        clienteDTO.setSenha(null); // Nunca retorne a senha
        return clienteDTO;
    }

    public VeterinarioDTO registerVeterinario(VeterinarioDTO veterinarioDTO) {
        if (this.usuarioRepository.findByEmail(veterinarioDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado.");
        }
        String encryptedPassword = passwordEncoder.encode(veterinarioDTO.getSenha());
        Veterinario newVeterinario = new Veterinario(
                veterinarioDTO.getEmail(),
                encryptedPassword,
                veterinarioDTO.getNomeCompleto(),
                veterinarioDTO.getCrmv(),
                veterinarioDTO.getEspecialidade()
        );
        Veterinario savedVeterinario = usuarioRepository.save(newVeterinario);

        // Retorna o DTO com o ID gerado e a senha nula
        veterinarioDTO.setId(savedVeterinario.getId());
        veterinarioDTO.setSenha(null); // Nunca retorne a senha
        return veterinarioDTO;
    }

    public UsuarioDTO registerUserWithRole(UsuarioRegisterDTO usuarioRegisterDTO) {
        if (this.usuarioRepository.findByEmail(usuarioRegisterDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado.");
        }
        String encryptedPassword = passwordEncoder.encode(usuarioRegisterDTO.getSenha());

        Usuario newUser;
        switch (usuarioRegisterDTO.getRole()) {
            case ADMIN:
                // Admin pode ter apenas nome, email, senha e role. Sem campos específicos de Cliente/Vet.
                newUser = new Usuario(
                        usuarioRegisterDTO.getEmail(),
                        encryptedPassword,
                        UsuarioRole.ADMIN,
                        usuarioRegisterDTO.getNomeCompleto()
                );
                break;
            case SECRETARIO:
                // Secretario pode ter apenas nome, email, senha e role.
                newUser = new Secretario(
                        usuarioRegisterDTO.getEmail(),
                        encryptedPassword,
                        usuarioRegisterDTO.getNomeCompleto()
                );
                break;
            case VETERINARIO:
                newUser = new Veterinario(
                        usuarioRegisterDTO.getEmail(),
                        encryptedPassword,
                        usuarioRegisterDTO.getNomeCompleto(),
                        usuarioRegisterDTO.getCrmv(),
                        usuarioRegisterDTO.getEspecialidade()
                );
                break;
            case CLIENTE:
                newUser = new Cliente(
                        usuarioRegisterDTO.getEmail(),
                        encryptedPassword,
                        usuarioRegisterDTO.getNomeCompleto(),
                        usuarioRegisterDTO.getTelefone(),
                        usuarioRegisterDTO.getCpf()
                );
                break;
            default:
                throw new IllegalArgumentException("Role inválida: " + usuarioRegisterDTO.getRole());
        }

        Usuario savedUser = usuarioRepository.save(newUser);

        // --- INÍCIO DO AJUSTE: Popula o DTO de resposta com base na ENTIDADE SALVA ---
        UsuarioDTO responseDTO = new UsuarioDTO();
        responseDTO.setId(savedUser.getId());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setNomeCompleto(savedUser.getNome());
        responseDTO.setRole(savedUser.getRole());

        // Popula campos específicos apenas se a instância salva for do tipo correto
        if (savedUser instanceof Cliente) {
            Cliente cliente = (Cliente) savedUser;
            responseDTO.setTelefone(cliente.getTelefone());
            responseDTO.setCpf(cliente.getCpf());
            // Se Cliente tiver endereço, adicione aqui: responseDTO.setEndereco(cliente.getEndereco());
        } else if (savedUser instanceof Veterinario) {
            Veterinario veterinario = (Veterinario) savedUser;
            responseDTO.setCrmv(veterinario.getCrmv());
            responseDTO.setEspecialidade(veterinario.getEspecialidade());
            // Se Veterinario tiver telefone, adicione aqui: responseDTO.setTelefone(veterinario.getTelefone());
            // Se Veterinario tiver endereço, adicione aqui: responseDTO.setEndereco(veterinario.getEndereco());
        }
        // Para ADMIN e SECRETARIO, não há campos específicos além de nome, email e role.
        // --- FIM DO AJUSTE ---

        return responseDTO;
    }
}