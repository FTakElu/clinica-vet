package com.clinicaveterinaria.clinicaveterinaria.config;

import com.clinicaveterinaria.clinicaveterinaria.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Necessário para H2 Console
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Liberar H2 Console e Swagger
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // --- Rotas Públicas (sem autenticação/autorização) ---
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register/cliente").permitAll()

                        // Permite requisições OPTIONS para todos os endpoints, o que é crucial para CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Rotas Protegidas por Role ---

                        // ADMIN: Tem acesso total ao gerenciamento de usuários (incluindo secretários e outros admins)
                        .requestMatchers(HttpMethod.POST, "/auth/register/user-with-role").hasRole("ADMIN")

                        // AGORA: SECRETARIO TAMBÉM PODE LISTAR E ATUALIZAR USUÁRIOS GENÉRICOS (Clientes/Veterinários)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasAnyRole("ADMIN", "SECRETARIO") // Listar todos os usuários
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").hasAnyRole("ADMIN", "SECRETARIO") // Buscar usuário por ID
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/{id}").hasAnyRole("ADMIN", "SECRETARIO") // Atualizar usuário
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/{id}").hasRole("ADMIN") // APENAS ADMIN PODE DELETAR

                        // SECRETARIO: Pode registrar Veterinários via rota AuthController
                        .requestMatchers(HttpMethod.POST, "/auth/register/veterinario").hasAnyRole("ADMIN", "SECRETARIO")

                        // Rotas específicas de gerenciamento de entidades (clientes, pets, consultas, etc.)
                        // Note que alguns já podem ter /api/ no seu Controller, outros podem não ter.
                        // Verifique seus controladores para confirmar os mapeamentos corretos.
                        .requestMatchers("/api/clientes/**").hasAnyRole("ADMIN", "SECRETARIO")
                        .requestMatchers("/api/pets/**").hasAnyRole("ADMIN", "SECRETARIO", "VETERINARIO", "CLIENTE")
                        .requestMatchers("/api/consultas/**").hasAnyRole("ADMIN", "SECRETARIO", "VETERINARIO", "CLIENTE")
                        .requestMatchers("/api/tipos-vacina/**").hasAnyRole("ADMIN", "SECRETARIO", "VETERINARIO")
                        .requestMatchers("/api/vacinas-aplicadas/**").hasAnyRole("ADMIN", "SECRETARIO", "VETERINARIO")

                        // VETERINARIO: Vê suas consultas, registra vacinas, gera relatórios
                        .requestMatchers(HttpMethod.POST, "/api/relatorios-consulta").hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/relatorios-consulta/**").hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.GET, "/api/relatorios-consulta/**").hasAnyRole("ADMIN", "SECRETARIO", "VETERINARIO", "CLIENTE")

                        // CLIENTE: Acessa seus próprios dados, pets e consultas
                        .requestMatchers("/api/meus-dados-cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/api/meus-pets/**").hasRole("CLIENTE")
                        .requestMatchers("/api/minhas-consultas/**").hasRole("CLIENTE")
                        .requestMatchers("/api/agendar-consulta").hasRole("CLIENTE")


                        // --- Qualquer outra requisição requer autenticação ---
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
