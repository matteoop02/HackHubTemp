package unicam.ids.HackHub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.model.UserRole;
import unicam.ids.HackHub.repository.UserRoleRepository;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRole findUserRoleById(Long userRoleId) {
        return userRoleRepository.findById(userRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Ruolo non trovato"));
    }

    public UserRole getDefaultUserRole() {
        return UserRole.builder()
                .id(1L)
                .name("UTENTE")
                .isActive(true)
                .build();
    }
}
