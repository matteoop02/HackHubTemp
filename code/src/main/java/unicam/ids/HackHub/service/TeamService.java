package unicam.ids.HackHub.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.team.CreateTeamRequest;
import unicam.ids.HackHub.dto.responses.TeamMemberResponse;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.model.UserRole;
import unicam.ids.HackHub.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final UserService userService;

    @Transactional
    public void createTeam(Authentication authentication, CreateTeamRequest request) {
        // Recupero l'utente autenticato
        User user = userService.findUserByUsername(authentication.getName());

        if (existsByName(request.name()))
            throw new IllegalArgumentException("Nome team già in uso!");

        // Creo il nuovo team
        Team team = Team.builder()
                .name(request.name())
                .teamLeader(user)
                .isPublic(request.isPublic())
                .build();

        // Aggiungo l'utente alla lista dei membri
        team.setMembers(new ArrayList<>());
        team.setMentors(new ArrayList<>());

        team.getMembers().add(user);

        userService.changeRole(user, 3L); // Cambio ruolo all'utente in Leader del Team
        userService.assignTeamToUser(user.getUsername(), team); // Assegno il team all'utente

        userService.save(user);
        save(team);
    }

    @Transactional
    public void addMemberToTeam(Team team, User user, UserRole proposedRole) {
        if (team.getMembers().contains(user))
            throw new IllegalArgumentException("Utente è già nel team");

        team.getMembers().add(user); // aggiungi l'utente

        if (proposedRole.equals(team.getTeamLeader().getRole())) {
            userService.changeRole(team.getTeamLeader(), 2L); // Il leader diventa membro
            userService.changeRole(user, 3L); // User diventa leader
        } else
            userService.changeRole(user, 2L); // Set all'utente il ruolo proposto

        userService.assignTeamToUser(user.getUsername(), team); // Assegno il team all'utente

        save(team); // salva il team
    }

    @Transactional
    public void joinToTeam(Authentication authentication, String teamName) {
        Team team = findByName(teamName);

        if (!team.isPublic())
            throw new IllegalArgumentException("Impossibile unirsi a un team privato!");

        User user = userService.findUserByUsername(authentication.getName());
        team.addMember(user);

        userService.changeRole(user, 2L); // Set all'utente il ruolo di membro
        userService.assignTeamToUser(user.getUsername(), team); // Assegno il team all'utente

        save(team);
    }

    @Transactional
    public void deleteMemberToTeam(Authentication authentication, String memberUsername) {
        User leader = userService.findUserByUsername(authentication.getName());
        User member = userService.findUserByUsername(memberUsername);
        Team team = leader.getTeam();

        if (!team.getMembers().contains(member))
            throw new IllegalArgumentException("Utente non è nel team");

        team.getMembers().remove(member); // rimozione l'utente
        save(team); // salva il team
    }

    @Transactional
    public void leaveTeam(Authentication authentication, @Valid String teamName) {
        Team team = findByName(teamName);

        User user = userService.findUserByUsername(authentication.getName());
        team.removeMember(user);

        userService.changeRole(user, 1L); // Set all'utente il ruolo di utente

        save(team);
    }

    public boolean existsByName(String nameTeam) {
        return teamRepository.existsByName(nameTeam);
    }

    public Team findByName(String teamName) {
        return teamRepository.findByName(teamName)
                .orElseThrow(() -> new ResourceNotFoundException("Team non trovato"));
    }

    public void save(Team team) {
        teamRepository.save(team);
    }

    public List<TeamMemberResponse> getTeamMembers(Authentication authentication) {
        User user = userService.findUserByUsername(authentication.getName());
        if (user.getTeam() == null) {
            throw new IllegalArgumentException("L'utente non appartiene a nessun team");
        }
        return user.getTeam().getMembers().stream()
                .map(u -> new TeamMemberResponse(
                        u.getUsername(),
                        u.getName(),
                        u.getSurname(),
                        u.getEmail(),
                        u.getRole().getName()))
                .toList();
    }

}
