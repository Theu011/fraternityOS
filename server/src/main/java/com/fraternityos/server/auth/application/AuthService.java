package com.fraternityos.server.auth.application;

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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use cases for self-signup (account only) and login. */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final MembershipPositionRepository membershipPositionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       MembershipRepository membershipRepository,
                       MembershipPositionRepository membershipPositionRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.membershipPositionRepository = membershipPositionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Creates a user account only. The new user belongs to no house yet — house
     * context (membership, positions) is established later through onboarding
     * (create or join a house). The issued token therefore carries no house.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User user = userRepository.save(new User(
                request.name(), email, passwordEncoder.encode(request.password())));

        List<String> positions = List.of();
        String token = jwtService.generateToken(
                user.getId(), null, null, email, positions);
        return AuthResponse.bearer(token, user.getId(), null, null, user.getName(), positions);
    }

    /** Verifies credentials and issues a token carrying the user's house context, if any. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        Optional<Membership> membership = membershipRepository.findByUserId(user.getId());
        Long membershipId = membership.map(Membership::getId).orElse(null);
        Long houseId = membership.map(Membership::getHouseId).orElse(null);
        List<String> positions = membershipId == null
                ? List.of()
                : membershipPositionRepository.findPositionNames(membershipId);

        String token = jwtService.generateToken(
                user.getId(), membershipId, houseId, user.getEmail(), positions);
        return AuthResponse.bearer(token, user.getId(), membershipId, houseId,
                user.getName(), positions);
    }
}
