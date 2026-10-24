package com.curso.mercat.service;

import com.curso.mercat.dto.AuthResponse;
import com.curso.mercat.dto.LoginRequest;
import com.curso.mercat.dto.RegisterRequest;
import com.curso.mercat.model.Role;
import com.curso.mercat.model.User;
import com.curso.mercat.repository.UserRepository;
import com.curso.mercat.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_USER // Los nuevos registros entran como usuarios estándar
        );

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getEmail(), user.getRole().name());
    }
}
