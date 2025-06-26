package com.clinicaveterinaria.clinicaveterinaria.model.entity;

import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "clientes")
@PrimaryKeyJoinColumn(name = "id") // Coluna de junção com a tabela 'usuarios'
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Cliente extends Usuario {

    // O campo 'nomeCompleto' é herdado da classe Usuario (como 'nome').
    // Não precisa ser declarado aqui para evitar duplicação.

    private String telefone;
    private String cpf;

    @OneToMany(mappedBy = "dono", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pet> pets;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consulta> consultas;

    public Cliente(String email, String senha, String nomeCompleto, String telefone, String cpf) {
        // Chamada ao construtor da superclasse Usuario: super(email, senha, role, nome)
        super(email, senha, UsuarioRole.CLIENTE, nomeCompleto);
        this.telefone = telefone;
        this.cpf = cpf;
    }
}
