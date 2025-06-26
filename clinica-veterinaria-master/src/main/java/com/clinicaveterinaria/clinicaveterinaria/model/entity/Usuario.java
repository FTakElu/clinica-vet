package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED) // Estratégia para herança
@Data // Lombok para getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok para construtor sem argumentos (necessário pelo JPA)
public class Usuario implements UserDetails { // Removido 'abstract'

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING) // Armazena o nome da enum como String no DB
    @Column(nullable = false)
    private UsuarioRole role;

    @Column(nullable = false) // Campo 'nome' adicionado
    private String nome;

    // Construtor para a criação de usuários base (como ADMIN)
    public Usuario(String email, String senha, UsuarioRole role, String nome) {
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.nome = nome;
    }

    // --- Métodos de UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == UsuarioRole.ADMIN) {
            // ADMIN tem todas as authorities para acessar qualquer endpoint
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_VETERINARIO"),
                    new SimpleGrantedAuthority("ROLE_SECRETARIO"),
                    new SimpleGrantedAuthority("ROLE_CLIENTE")
            );
        }
        // Para outras roles, retorna apenas a sua própria autoridade (ex: ROLE_CLIENTE)
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}