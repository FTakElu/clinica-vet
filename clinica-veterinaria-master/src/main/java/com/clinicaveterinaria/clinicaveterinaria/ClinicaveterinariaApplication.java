package com.clinicaveterinaria.clinicaveterinaria; // Ajuste o pacote conforme necessário

import com.clinicaveterinaria.clinicaveterinaria.model.entity.Usuario;
import com.clinicaveterinaria.clinicaveterinaria.repository.UsuarioRepository;
import com.clinicaveterinaria.clinicaveterinaria.model.enums.UsuarioRole; // Usar UsuarioRole (seu enum)
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ClinicaveterinariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClinicaveterinariaApplication.class, args);
	}

	@Bean
	public CommandLineRunner createAdminUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Verifica se o usuário admin já existe pelo email
			if (usuarioRepository.findByEmail("admin@gmail.com").isEmpty()) {
				// Instancia Usuario diretamente, pois agora não é mais abstrato
				Usuario admin = new Usuario(
						"admin@gmail.com",
						passwordEncoder.encode("123123"), // Senha codificada
						UsuarioRole.ADMIN, // Define a role
						"Administrador Principal" // Define o nome
				);

				usuarioRepository.save(admin);
				System.out.println("Usuário admin criado com sucesso (email: admin@gmail.com, senha: 123123)!");
			} else {
				System.out.println("Usuário admin já existe.");
			}
		};
	}
}
