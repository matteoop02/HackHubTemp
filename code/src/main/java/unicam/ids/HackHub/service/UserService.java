package unicam.ids.HackHub.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.model.UserRole;
import unicam.ids.HackHub.repository.TeamRepository;
import unicam.ids.HackHub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import unicam.ids.HackHub.dto.requests.invite.RegisterFromInviteRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
    }

    public boolean existsUserByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void changeRole(User user, Long roleId) {
        user.setRole(userRoleService.findUserRoleById(roleId));
    }

    @Transactional
    public void assignTeamToUser(String username, Team team) {
        User user = this.findUserByUsername(username);
        user.setTeam(team);
    }

    @Transactional
    public User registerUserFromInvite(RegisterFromInviteRequest request, String email) {
        User user = User.builder()
                .username(request.username())
                .email(email)
                .name(request.name())
                .surname(request.surname())
                .password(passwordEncoder.encode(request.password()))
                .dateOfBirth(request.dateOfBirth())
                .role(userRoleService.findUserRoleById(1L))
                .isDeleted(false)
                .build();

        return userRepository.save(user);
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
