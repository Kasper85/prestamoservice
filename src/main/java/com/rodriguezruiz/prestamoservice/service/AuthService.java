package com.rodriguezruiz.prestamoservice.service;

import com.rodriguezruiz.prestamoservice.config.GlobalExceptionHandler.CredencialesInvalidasException;
import com.rodriguezruiz.prestamoservice.config.GlobalExceptionHandler.EmailDuplicadoException;
import com.rodriguezruiz.prestamoservice.dto.AuthResponse;
import com.rodriguezruiz.prestamoservice.dto.LoginRequest;
import com.rodriguezruiz.prestamoservice.dto.RegisterRequest;
import com.rodriguezruiz.prestamoservice.entity.Usuario;
import com.rodriguezruiz.prestamoservice.repository.UsuarioRepository;
import com.rodriguezruiz.prestamoservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailDuplicadoException("El email ya está registrado");
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                email,
                passwordEncoder.encode(request.password()),
                "USER"
        );
        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas"));

        String token = jwtService.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getNombre(), usuario.getEmail());
    }
}
