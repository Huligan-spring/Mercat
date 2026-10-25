package com.curso.mercat.service;

import com.curso.mercat.dto.AuthResponse;
import com.curso.mercat.dto.LoginRequest;
import com.curso.mercat.dto.RegisterRequest;
import com.curso.mercat.model.Role;
import com.curso.mercat.model.User;
import com.curso.mercat.repository.UserRepository;
import com.curso.mercat.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    // ---------------------------------------------------------
    // register
    // ---------------------------------------------------------

    @Test
    @DisplayName("register: Lanza IllegalArgumentException si el email ya existe")
    void register_EmailAlreadyExists_ShouldThrowException() {
        RegisterRequest request = new RegisterRequest("existente@mercat.com", "password123");
        when(userRepository.existsByEmail("existente@mercat.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El email ya está registrado");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("register: Registro exitoso cifra contraseña, asigna ROLE_USER, persiste y emite JWT")
    void register_Success_ShouldEncodePasswordAssignRoleAndReturnToken() {
        RegisterRequest request = new RegisterRequest("nuevo@mercat.com", "rawPassword");
        when(userRepository.existsByEmail("nuevo@mercat.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword123");
        when(jwtService.generateToken(any(User.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authenticationService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getEmail()).isEqualTo("nuevo@mercat.com");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_USER.name());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("nuevo@mercat.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword123");
        assertThat(savedUser.getRole()).isEqualTo(Role.ROLE_USER);
    }

    // ---------------------------------------------------------
    // login
    // ---------------------------------------------------------

    @Test
    @DisplayName("login: Lanza BadCredentialsException si las credenciales son inválidas")
    void login_InvalidCredentials_ShouldPropagateException() {
        LoginRequest request = new LoginRequest("user@mercat.com", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Credenciales incorrectas");

        verify(userRepository, never()).findByEmail(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login: Lanza IllegalArgumentException si el usuario no existe tras autenticación")
    void login_UserNotFound_ShouldThrowException() {
        LoginRequest request = new LoginRequest("inexistente@mercat.com", "password123");
        when(userRepository.findByEmail("inexistente@mercat.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email o contraseña incorrectos");

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("login: Inicio de sesión exitoso autentica con AuthenticationManager y emite JWT")
    void login_Success_ShouldAuthenticateAndReturnAuthResponse() {
        LoginRequest request = new LoginRequest("admin@mercat.com", "admin123");
        User user = new User("admin@mercat.com", "hashedAdminPass", Role.ROLE_ADMIN);

        when(userRepository.findByEmail("admin@mercat.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("admin.jwt.token");

        AuthResponse response = authenticationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("admin.jwt.token");
        assertThat(response.getEmail()).isEqualTo("admin@mercat.com");
        assertThat(response.getRole()).isEqualTo(Role.ROLE_ADMIN.name());

        verify(authenticationManager).authenticate(
                argThat(auth -> auth.getPrincipal().equals("admin@mercat.com") &&
                                auth.getCredentials().equals("admin123"))
        );
        verify(jwtService).generateToken(user);
    }
}
