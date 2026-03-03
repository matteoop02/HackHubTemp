package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.hackathon.*;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.service.HackathonService;
import unicam.ids.HackHub.model.declareWinner.HighestScoreStrategy;

import java.util.List;

@RestController
@RequestMapping("/api/hackathon")
@RequiredArgsConstructor
@Tag(name = "Hackathon", description = "Gestione degli hackathon")
public class HackathonController {

    private final HackathonService hackathonService;

    // --------------------------------- GET VARI ---------------------------------

    @GetMapping("/public")
    @Operation(summary = "Consultazione Hackathon", description = """
                Restituisce la lista degli hackathon.
                Se l'utente è autenticato → ritorna tutti gli hackathon (pubblici + privati).
                Se l'utente non è autenticato → ritorna solo gli hackathon pubblici.
            """)
    @ApiResponse(responseCode = "200", description = "Lista hackathon ottenuta con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<List<Hackathon>> getHackathonsList(Authentication authentication) {
        try {
            // Se l'utente è autenticato, restituisci tutti gli hackathon, sia privati che
            // pubblici
            if (authentication != null && authentication.isAuthenticated())
                return ResponseEntity.ok(hackathonService.getHackathons());

            // Altrimenti restituisci solo quelli pubblici
            return ResponseEntity.ok(hackathonService.getHackathonsPublic());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public/info")
    @Operation(summary = "Visualizza informazioni Hackathon", description = """
                Restituisce le informazioni di un hackathon.
                Se l'utente è autenticato → ritorna le informazioni di un hackathon (pubblici + privati).
                Se l'utente non è autenticato → ritorna solo le informazioni gli hackathon pubblici.
            """)
    @ApiResponse(responseCode = "200", description = "Informazioni hackathon ottenute con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<Hackathon> getHackathonInfo(Authentication authentication,
            @RequestBody @Valid HackathonInfoRequest hackathonInfoRequest) {
        try {
            if (authentication != null && authentication.isAuthenticated())
                return ResponseEntity.ok(hackathonService.findHackathonInfo(hackathonInfoRequest.id()));

            return ResponseEntity.ok(hackathonService.findPublicHackathonInfo(hackathonInfoRequest.id()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/giudice/getSubmissions")
    @Operation(summary = "Visualizza le sottomissioni di un Hackathon", description = """
                Restituisce le sottomissioni di un hackathon.
            """)
    @ApiResponse(responseCode = "200", description = "Informazioni hackathon ottenute con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<List<Submission>> getHackathonSubmissions(Authentication authentication) {
        try {
            return ResponseEntity.ok(hackathonService.getHackathonSubmissions(authentication));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // --------------------------------- CREA HACKATHON
    // ---------------------------------

    @PostMapping("/organizzatore/create")
    @Operation(summary = "Creazione nuovo hackathon", description = "Permette la registrazione di un nuovo hackathon", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati dell'hackathon da registrare", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio registrazione", value = """
                {
                  "name": "Hackathon Innovazione 2026",
                  "place": "Camerino",
                  "regulation": "Lorem ipsum dolor sit amet...",
                  "subscriptionDeadline": "2026-04-01T23:59:59",
                  "startDate": "2026-05-01T23:59:59",
                  "endDate": "2026-05-03T23:59:59",
                  "reward": 5000.0,
                  "maxTeamSize": 5,
                  "isPublic": true
                }
            """))))
    @ApiResponse(responseCode = "200", description = "Utente registrato con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida o dati mancanti")
    public ResponseEntity<String> createHackathon(Authentication authentication,
            @RequestBody @Valid CreateHackathonRequest createHackathonRequest) {
        try {
            hackathonService.createHackathon(authentication, createHackathonRequest);
            return ResponseEntity.ok("Hackathon creato con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Errore! Creazione Hackathon fallita! \n" + ex.getMessage());
        }
    }

    // --------------------------------- MODIFICHE HACKATHON
    // ---------------------------------

    @PostMapping("/organizzatore/updateStartDate")
    @Operation(summary = "Aggiorno data inizio", description = """
                Permette ad un organizzatore di aggiornare la data di inizio hackathon.
                Di conseguenza modifica la scadenza dell'invio delle sottomissioni.
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per l'aggiornamento della data di inizio", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio iscrizione giudice", value = """
            {
                "startDate": "2026-04-08T23:59:59"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Data di inzio aggiornata con successo")
    @ApiResponse(responseCode = "400", description = "Errore durante l'aggiornamento della data")
    public ResponseEntity<String> updateStartDate(
            @RequestBody @Valid UpdateHackathonStartAndEndDateRequest updateHackathonStartDateRequest) {
        try {
            hackathonService.updateStartDate(updateHackathonStartDateRequest);
            return ResponseEntity.ok("Data aggiornata con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body("Aggiornamento data di inizio hackathon fallito! " + ex.getMessage());
        }
    }

    // --------------------------------- GESTIONE TEAM
    // ---------------------------------

    @PostMapping("/leaderDelTeam/signTeam")
    @Operation(summary = "Iscrizione Team ad un Hackathon", description = """
                Permette ad un team di iscriversi ad un hackathon.
                Richiede username del team leader e nome dell'hackathon.
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per l'iscrizione del team", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio iscrizione team", value = """
                {
                  "hackathonName": "Hackathon Innovazione 2026"
                }
            """))))
    @ApiResponse(responseCode = "200", description = "Team iscritto con successo all'hackathon")
    @ApiResponse(responseCode = "400", description = "Errore durante l'iscrizione del team")
    public ResponseEntity<String> signTeam(Authentication authentication,
            @RequestBody @Valid SignTeamRequest signTeamRequest) {

        try {
            hackathonService.signTeamToHackathon(authentication, signTeamRequest);
            return ResponseEntity.ok("Team iscritto all'hackathon con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Iscrizione del team all'hackathon fallita! " + ex.getMessage());
        }
    }

    @PostMapping("/leaderDelTeam/unsubscribeTeam")
    @Operation(summary = "Disiscrizione Team da un Hackathon", description = """
                Permette ad un team di disiscriversi da un hackathon.
                Richiede l'operazione venga fatta dal leader del team.
            """)
    @ApiResponse(responseCode = "200", description = "Team disiscritto con successo dall'hackathon")
    @ApiResponse(responseCode = "400", description = "Errore durante la disiscrizione del team")
    public ResponseEntity<String> unsubscribeTeam(Authentication authentication) {
        try {
            hackathonService.unsubscribeTeamToHackathon(authentication);
            return ResponseEntity.ok("Team disiscritto dall'hackathon con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body("Disiscrizione del team dall'hackathon fallita! " + ex.getMessage());
        }
    }

    // --------------------------------- GESTIONE STAFF
    // ---------------------------------

    @PostMapping("/organizzatore/setJudge")
    @Operation(summary = "Aggiunta di un giudice ad un Hackathon", description = """
                Permette ad un organizzatore di aggiungere un giudice.
                Richiede username del giudice e nome dell'hackathon.
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per l'iscrizione del giudice", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio iscrizione giudice", value = """
            {
                "hackathonName": "Hackathon Innovazione 2026"
                "judgeUsername": "Andrea"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Giudice iscritto con successo all'hackathon")
    @ApiResponse(responseCode = "400", description = "Errore durante l'iscrizione del giudice")
    public ResponseEntity<String> setJudge(@RequestBody @Valid SetJudgeToHackathonRequest setJudgeToHackathonRequest) {
        try {
            hackathonService.setJudge(setJudgeToHackathonRequest);
            return ResponseEntity.ok("Giudice iscritto all'hackathon con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Iscrizione del giudice all'hackathon fallita! " + ex.getMessage());
        }
    }

    @PostMapping("/organizzatore/removeJudge")
    @Operation(summary = "Rimozione del giudice da un Hackathon", description = """
                Permette ad un organizzatore di rimuovere il giudice assegnato ad un hackathon.
                Richiede nome dell'hackathon.
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Nome dell'hackathon da cui rimuovere il giudice", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio rimozione giudice", value = """
                "Hackathon Innovazione 2026"
            """))))
    @ApiResponse(responseCode = "200", description = "Giudice rimosso con successo dall'hackathon")
    @ApiResponse(responseCode = "400", description = "Errore durante la rimozione del giudice")
    public ResponseEntity<String> removeJudge(
            @RequestBody @Valid RemoveJudgeFromHackathonRequest removeJudgeFromHackathonRequest) {
        try {
            hackathonService.removeJudge(removeJudgeFromHackathonRequest);
            return ResponseEntity.ok("Giudice rimosso dall'hackathon con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Rimozione del giudice fallita! " + ex.getMessage());
        }
    }

    @PostMapping("/organizzatore/addMentor")
    @Operation(summary = "Aggiunta di un mentore ad un Hackathon", description = """
                Permette ad un organizzatore di aggiungere un mentore.
                Richiede username del mentore e nome dell'hackathon.
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per l'iscrizione del mentore", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio iscrizione giudice", value = """
            {
                "mentorUsername": "Marco"
                "hackathonName": "Hackathon Innovazione 2026"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Mentore iscritto con successo all'hackathon")
    @ApiResponse(responseCode = "400", description = "Errore durante l'iscrizione del mentore")
    public ResponseEntity<String> addMentor(
            @RequestBody @Valid AddMentorToHackathonRequest addMentorToHackathonRequest) {
        try {
            hackathonService.addMentor(addMentorToHackathonRequest);
            return ResponseEntity.ok("Mentore iscritto all'hackathon con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Iscrizione del mentore all'hackathon fallita! " + ex.getMessage());
        }
    }

    @PostMapping("/organizzatore/removeMentor")
    @Operation(summary = "Rimovìzione di un mentore ad un Hackathon", description = """
                Permette ad un organizzatore di rimuovere un mentore.
                Richiede username del mentore e nome dell'hackathon.
            """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per la rimozione del mentore", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio iscrizione giudice", value = """
            {
                "mentorUsername": "Filipp"
                "hackathonName": "Hackathon Innovazione 2026"
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Mentore rimosso con successo all'hackathon")
    @ApiResponse(responseCode = "400", description = "Errore durante la rimozione del mentore")
    public ResponseEntity<String> removeMentor(
            @RequestBody @Valid RemoveMentorFromHackathonRequest removeMentorFromHackathonRequest) {
        try {
            hackathonService.removeMentor(removeMentorFromHackathonRequest);
            return ResponseEntity.ok("Mentore rimosso dall'hackathon con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Rimozione del mentore all'hackathon fallita! " + ex.getMessage());
        }
    }

    // --------------------------------- VINCITORE E PREMIO
    // ---------------------------------

    @PostMapping("/organizzatore/declareWinner")
    @Operation(summary = "Dichiara il vincitore dell'hackathon", description = "Proclama automaticamente il vincitore in base al punteggio più alto", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio proclamazione automatica", value = """
                {
                  "hackathonName": "Hackathon Unicam 2026"
                }
            """))))
    @ApiResponse(responseCode = "200", description = "Vincitore proclamato con successo")
    @ApiResponse(responseCode = "400", description = "Richiesta non valida")
    public ResponseEntity<String> declareWinner(@RequestBody @Valid DeclareWinnerRequest request) {
        try {
            hackathonService.declareWinner(request.hackathonName(), new HighestScoreStrategy());
            return ResponseEntity.ok("Vincitore proclamato automaticamente in base al punteggio più alto!");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Richiesta non valida! " + ex.getMessage());
        }
    }
}