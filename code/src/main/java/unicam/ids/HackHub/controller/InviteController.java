package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import unicam.ids.HackHub.dto.requests.invite.*;
import unicam.ids.HackHub.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import unicam.ids.HackHub.service.InviteService;
import java.util.List;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
@Tag(name = "Inviti", description = "Gestione degli inviti: Interni (da e verso i team) ed Esterni (per utenti invitati ad iscriversi alla piattaforma)")
public class InviteController {

    private final InviteService inviteService;

    // ------------------------------- OUTSIDE INVITE MANAGE
    // -------------------------------

    @PostMapping("/public/inviteOutsideUser")
    @Operation(summary = "Creazione nuovo invito esterno", description = "Permette l'invio di un nuovo invito esterno", requestBody = @RequestBody(description = "Dati dell'invito", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio invito", value = """
                {
                  "recipientName": "test@example.com",
                  "message": "Qualcosa"
                }
            """))))
    @ApiResponse(responseCode = "200", description = "Invito inviato con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<InviteOutsidePlatform> inviteOutsideUser(Authentication authentication,
            @RequestBody @Valid OutsideInviteRequest outsideInviteRequest) {
        try {
            InviteOutsidePlatform invite = inviteService.inviteOutsideUser(authentication, outsideInviteRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(invite);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/outside/acceptInviteAndRegisterUser")
    @Operation(summary = "Accetta invito esterno e registra l'utente", description = "Permette a un utente invitato via email di completare la registrazione usando il token dell’invito.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio registrazione da invito", value = """
            {
              "token": "abc123TOKENabc123",
              "username": "andrea.pistolesi",
              "name": "Andrea",
              "surname": "Pistolesi",
              "password": "123456789",
              "dateOfBirth": "2000-03-02"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Invito e registrazione avvenuti con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> acceptInviteAndRegisterUser(@RequestBody @Valid RegisterFromInviteRequest request) {
        try {
            inviteService.acceptInviteAndRegisterUser(request);
            return ResponseEntity.ok("Registrazione completata! Utente aggiunto alla piattaforma.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/outside/rejectOutsideInvite")
    @Operation(summary = "Rifiuta invito esterno.", description = "Permette a un utente di rifiutare l’invito esterno.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio di rifiuto.", value = """
            {
              "token": "abc123TOKENabc123",
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Rifiuto avvenuto con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> rejectOutsideInvite(
            @RequestBody @Valid RejectOutsideInviteRequest rejectOutsideInviteRequest) {
        try {
            inviteService.rejectOutsideInvite(rejectOutsideInviteRequest);
            return ResponseEntity.ok("Rifiuto dell'invito riuscito!");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Rifiuto dell'invito fallito!" + ex.getMessage());
        }
    }

    // ------------------------------- INSIDE INVITE MANAGE
    // -------------------------------

    @PostMapping("/leaderDelTeam/inviteToTeam")
    @Operation(summary = "Invita un utente al team", description = "Permette al leader di un team di invitare un utente già registrato a unirsi al team.")
    @ApiResponse(responseCode = "201", description = "Invito al team creato con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o utente non valido")
    public ResponseEntity<InviteInsidePlatform> inviteToTeam(
            @RequestBody @Valid InsideInviteRequest insideInviteRequest) {

        InviteInsidePlatform invite = inviteService.inviteUserToTeam(insideInviteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(invite);
    }

    @PostMapping("/inviteManage/acceptTeamInvite")
    @Operation(summary = "Accetta invito al team", description = "Permette a un utente autenticato di accettare un invito ricevuto per entrare in un team.")
    @ApiResponse(responseCode = "200", description = "Invito accettato con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante l'accettazione dell'invito")
    public ResponseEntity<String> acceptTeamInvite(Authentication authentication,
            @RequestBody @Valid AcceptTeamInvite acceptTeamInvite) {
        try {
            inviteService.acceptTeamInvite(authentication, acceptTeamInvite);
            return ResponseEntity.ok("Invito accettato con successo!");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Accettazione dell'invito fallita!\n" + ex.getMessage());
        }
    }

    @PostMapping("/inviteManage/rejectTeamInvite")
    @Operation(summary = "Rifiuta invito al team", description = "Permette a un utente autenticato di rifiutare un invito ricevuto per entrare in un team.")
    @ApiResponse(responseCode = "200", description = "Invito rifiutato con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante il rifiuto dell'invito")
    public ResponseEntity<String> rejectTeamInvite(Authentication authentication, @RequestParam Long inviteId) {
        try {
            inviteService.rejectTeamInvite(authentication, inviteId);
            return ResponseEntity.ok("Invito accettato con successo!");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Accettazione dell'invito fallita!\n" + ex.getMessage());
        }
    }

    // ------------------------------- GET INVITE -------------------------------

    @GetMapping("/inviteManage/senderEmail")
    @Operation(summary = "Recupera inviti pendenti dell'utente", description = "Restituisce la lista degli inviti pendenti ricevuti dall'utente autenticato.")
    public ResponseEntity<List<InviteInsidePlatform>> findPendingInvitesForEmails(Authentication authentication) {

        List<InviteInsidePlatform> invites = inviteService.findPendingInvitesForUser(authentication);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/inviteManage/user")
    @Operation(summary = "Recupera tutti gli inviti pendenti per l'utente", description = "Restituisce tutti gli inviti pendenti associati all'utente autenticato.")
    public ResponseEntity<List<InviteInsidePlatform>> findAllPendingInvitesForUsers(Authentication authentication) {

        List<InviteInsidePlatform> invites = inviteService.findPendingInvitesForUser(authentication);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/inviteManage/team")
    @Operation(summary = "Recupera inviti dei team", description = "Restituisce tutti gli inviti relativi ai team dell'utente autenticato.")
    public ResponseEntity<List<InviteInsidePlatform>> findAllTeamInvites(Authentication authentication) {
        List<InviteInsidePlatform> invites = inviteService.findTeamInvites(authentication);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/inviteManage/recipientEmail")
    @Operation(summary = "Recupera inviti per email destinatario", description = "Restituisce tutti gli inviti (interni ed esterni) associati a una determinata email.")
    public ResponseEntity<List<Invite>> findAllRecipientEmailInvites(@RequestParam String recipientEmail) {
        List<Invite> invites = inviteService.findEmailInvites(recipientEmail);
        return ResponseEntity.ok(invites);
    }
}
