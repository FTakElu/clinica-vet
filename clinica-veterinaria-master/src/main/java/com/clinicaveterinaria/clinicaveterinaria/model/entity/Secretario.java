package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "secretarios")
@PrimaryKeyJoinColumn(name = "id")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Secretario extends Usuario {

    // O campo 'nomeCompleto' é herdado da classe Usuario (como 'nome').
    // Não precisa ser declarado aqui para evitar duplicação.

    // Outros campos específicos do secretário, se houver

    public Secretario(String email, String senha, String nomeCompleto) {
        // Chamada ao construtor da superclasse Usuario: super(email, senha, role, nome)
        super(email, senha, UsuarioRole.SECRETARIO, nomeCompleto);
    }
}
