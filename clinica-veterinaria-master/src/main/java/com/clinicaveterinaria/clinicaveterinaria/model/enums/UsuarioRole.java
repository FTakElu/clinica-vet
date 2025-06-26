package com.clinicaveterinaria.clinicaveterinaria.model.enums;

import lombok.Getter;

@Getter
public enum UsuarioRole {
    ADMIN("admin"),
    CLIENTE("cliente"),
    SECRETARIO("secretario"),
    VETERINARIO("veterinario");

    private String role;

    UsuarioRole(String role) {
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}
