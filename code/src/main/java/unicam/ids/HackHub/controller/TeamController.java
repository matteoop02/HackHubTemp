package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.team.CreateTeamRequest;
import unicam.ids.HackHub.dto.requests.team.JoinToTeamRequest;
import unicam.ids.HackHub.dto.requests.team.LeaveTeamRequest;
import unicam.ids.HackHub.dto.requests.team.RemoveMemberToTeamRequest;
import unicam.ids.HackHub.dto.responses.TeamMemberResponse;
import unicam.ids.HackHub.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Gestione dinamiche dei team")
public class TeamController {
    private final TeamService teamService;

    @PostMapping("/utente/create")
    @Operation(summary = "Creazione nuovo team", description = "Permette la registrazione di un nuovo team da parte di un Utente")
    @ApiResponse(responseCode = "200", description = "Team registrato con successo")
    @ApiResponse(responseCode = "400", description = "Team non creato,errore")
    public ResponseEntity<String> createTeam(Authentication authentication,
            @RequestBody @Valid CreateTeamRequest createTeamRequest) {
        try {
            teamService.createTeam(authentication, createTeamRequest);
            return ResponseEntity.ok("Team creato con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/utente/joinToTeam")
    @Operation(summary = "Aggiunge l'utente loggato al team se pubblico", description = """
                Un utente può unirsi a un team pubblico.
            """)
    @ApiResponse(responseCode = "200", description = "Partecipazione avvenuta con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> joinToTeam(Authentication authentication,
            @RequestBody @Valid JoinToTeamRequest joinToTeamRequest) {
        try {
            teamService.joinToTeam(authentication, joinToTeamRequest.teamName());
            return ResponseEntity.ok("Unione al team effettuata con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/leaderDelTeam/remove")
    @Operation(summary = "Rimuovi membro dal team", description = """
                Rimuove un membro del team.
            """)
    @ApiResponse(responseCode = "200", description = "Rimozione del membro avvenuta con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> removeMemberToTeam(Authentication authentication,
            @RequestBody @Valid RemoveMemberToTeamRequest removeMemberToTeamRequest) {
        try {
            teamService.deleteMemberToTeam(authentication, removeMemberToTeamRequest.member());
            return ResponseEntity.ok("Membro rimosso con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/membroDelTeam/leaveTeam")
    @Operation(summary = "Rimuove l'utente loggato dal team", description = """
                Un utente può lasciare il team.
            """)
    @ApiResponse(responseCode = "200", description = "Partecipazione avvenuta con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> leaveTeam(Authentication authentication,
            @RequestBody @Valid LeaveTeamRequest leaveTeamRequest) {
        try {
            teamService.leaveTeam(authentication, leaveTeamRequest.teamName());
            return ResponseEntity.ok("La richiesta di lasciare il team è avvenuta con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/membroDelTeam/members")
    @Operation(summary = "Recupera membri del team dell'utente", description = "Restituisce la lista dei membri del team a cui l'utente autenticato appartiene.")
    @ApiResponse(responseCode = "200", description = "Lista dei membri del team recuperata con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante il recupero dei membri del team")
    public ResponseEntity<List<TeamMemberResponse>> getMyTeamMembers(Authentication authentication) {
        return ResponseEntity.ok(teamService.getTeamMembers(authentication));
    }

}
