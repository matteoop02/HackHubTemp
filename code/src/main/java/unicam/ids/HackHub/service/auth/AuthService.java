package unicam.ids.HackHub.service.auth;

import lombok.RequiredArgsConstructor;
import unicam.ids.HackHub.config.auth.CustomUserDetailsService;
import unicam.ids.HackHub.dto.requests.auth.LoginUserRequest;
import unicam.ids.HackHub.dto.requests.auth.RegisterUserRequest;
import unicam.ids.HackHub.dto.responses.AuthResponse;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.UserRepository;
import unicam.ids.HackHub.service.UserRoleService;
import unicam.ids.HackHub.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRoleService userRoleService;

    // Registra un utente
    @Transactional
    public void register(RegisterUserRequest request) {
        // Controlla unicità email e username
        if (userRepository.existsByUsername(request.username()))
            throw new IllegalArgumentException("Username già esistente" + request.username());

        // Crea l'utente
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setRole(userRoleService.findUserRoleById(request.roleId()));
        user.setDateOfBirth(request.dateOfBirth());

        // Salva l'utente
        userRepository.save(user);
    }

    /**
     * Esegue il login e restituisce un token JWT.
     */
    public AuthResponse login(LoginUserRequest request) {
        // 1. Autentica username + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        // 2. Carica l’utente tramite il CustomUserDetailsService
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.username());

        // 3. Genera token JWT
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }
}