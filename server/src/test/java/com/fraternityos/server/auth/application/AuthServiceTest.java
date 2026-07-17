package com.fraternityos.server.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fraternityos.server.auth.application.dto.AuthResponse;
import com.fraternityos.server.auth.application.dto.LoginRequest;
import com.fraternityos.server.auth.application.dto.RegisterRequest;
import com.fraternityos.server.house.domain.Membership;
import com.fraternityos.server.house.domain.User;
import com.fraternityos.server.house.infrastructure.MembershipPositionRepository;
import com.fraternityos.server.house.infrastructure.MembershipRepository;
import com.fraternityos.server.house.infrastructure.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link AuthService} with all collaborators mocked — asserts the
 * register/login use-case behaviour (hashing, email normalisation, house-context
 * resolution, credential failures) in isolation from Spring and the database.
 */
class AuthServiceTest {

    private UserRepository userRepository;
    private MembershipRepository membershipRepository;
    private MembershipPositionRepository membershipPositionRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        membershipPositionRepository = mock(MembershipPositionRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, membershipRepository,
                membershipPositionRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_createsHouselessAccount_andIssuesTokenWithoutHouseContext() {
        when(userRepository.existsByEmail("alice@x.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        User saved = mock(User.class);
        when(saved.getId()).thenReturn(1L);
        when(saved.getName()).thenReturn("Alice");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(eq(1L), isNull(), isNull(), eq("alice@x.com"), any()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.register(
                new RegisterRequest("Alice", "alice@x.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.membershipId()).isNull();
        assertThat(response.houseId()).isNull();
        assertThat(response.positions()).isEmpty();
    }

    @Test
    void register_normalisesEmailToLowercase_beforeStoringAndHashing() {
        when(userRepository.existsByEmail("alice@x.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(mock(User.class));

        authService.register(new RegisterRequest("Alice", "Alice@X.CoM", "password123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@x.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void register_existingEmail_throwsAndDoesNotSave() {
        when(userRepository.existsByEmail("alice@x.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("Alice", "alice@x.com", "password123")))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_unknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmail("ghost@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@x.com", "whatever")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("hashed");
        when(userRepository.findByEmail("mat@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("mat@x.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_memberWithPositions_issuesTokenCarryingHouseContext() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(user.getEmail()).thenReturn("mat@x.com");
        when(user.getName()).thenReturn("Mat");
        when(user.getPasswordHash()).thenReturn("hashed");
        when(userRepository.findByEmail("mat@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        Membership membership = mock(Membership.class);
        when(membership.getId()).thenReturn(3L);
        when(membership.getHouseId()).thenReturn(42L);
        when(membershipRepository.findByUserId(7L)).thenReturn(Optional.of(membership));
        when(membershipPositionRepository.findPositionNames(3L))
                .thenReturn(List.of("President", "Treasurer"));
        when(jwtService.generateToken(eq(7L), eq(3L), eq(42L), eq("mat@x.com"), any()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("mat@x.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.membershipId()).isEqualTo(3L);
        assertThat(response.houseId()).isEqualTo(42L);
        assertThat(response.positions()).containsExactly("President", "Treasurer");
    }

    @Test
    void login_userWithoutHouse_issuesTokenWithNoMembershipAndNoPositions() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(user.getEmail()).thenReturn("mat@x.com");
        when(user.getName()).thenReturn("Mat");
        when(user.getPasswordHash()).thenReturn("hashed");
        when(userRepository.findByEmail("mat@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(membershipRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(jwtService.generateToken(eq(7L), isNull(), isNull(), eq("mat@x.com"), any()))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("mat@x.com", "password123"));

        assertThat(response.membershipId()).isNull();
        assertThat(response.houseId()).isNull();
        assertThat(response.positions()).isEmpty();
        verify(membershipPositionRepository, never()).findPositionNames(any());
    }
}
