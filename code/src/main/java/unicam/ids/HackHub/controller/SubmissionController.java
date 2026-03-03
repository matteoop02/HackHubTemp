package unicam.ids.HackHub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.submission.*;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.service.SubmissionService;
import java.util.List;

@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
@Tag(name = "Sottomissioni", description = "Gestione delle sottomissioni")
public class SubmissionController {

    private final SubmissionService submissionService;

    // ------------------------------- STAFF -------------------------------

    /**
     * Recupera tutte le sottomissioni assegnate allo staff autenticato.
     */
    @GetMapping("/staff/listByStaffMember")
    @Operation(summary = "Lista sottomissioni per membro dello staff", description = "Recupera tutte le sottomissioni associate allo staff autenticato.")
    @ApiResponse(responseCode = "200", description = "Sottomissioni recuperate con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<List<Submission>> getSubmissionsListByHackathonName(Authentication authentication) {
        try {
            List<Submission> submissions = submissionService.getSubmissionsByStaffMember(authentication.getName());
            return ResponseEntity.ok(submissions);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Recupera tutte le sottomissioni di un determinato hackathon.
     */
    @GetMapping("/staff/listByHackathon")
    @Operation(summary = "Lista sottomissioni per hackathon", description = "Recupera tutte le sottomissioni associate a un hackathon specifico.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "hackathonName": "HackathonInnovazione2026"
            }
            """))))
    public ResponseEntity<List<Submission>> getSubmissionsListByHackathonName(
            @Valid @RequestBody SubmissionsListByHackathonRequest submissionsListByHackathonRequest) {
        try {
            List<Submission> submissions = submissionService.getSubmissionsByHackathonName(
                    submissionsListByHackathonRequest.hackathonName());
            return ResponseEntity.ok(submissions);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Recupera tutte le sottomissioni di un determinato team.
     */
    @GetMapping("/staff/listByTeam")
    @Operation(summary = "Lista sottomissioni per team", description = "Recupera tutte le sottomissioni associate a un team specifico.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
            {
              "teamName": "TeamInnovazione"
            }
            """))))
    public ResponseEntity<List<Submission>> getSubmissionsListByTeamName(
            @Valid @RequestBody SubmissionsListByTeamRequest submissionsListByTeamRequest) {
        try {
            List<Submission> submissions = submissionService.getSubmissionsByTeamName(
                    submissionsListByTeamRequest.teamName());
            return ResponseEntity.ok(submissions);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ------------------------------- TEAM -------------------------------

    /**
     * Crea una nuova sottomissione per un team.
     */
    @PostMapping("/team/create")
    @Operation(summary = "Crea sottomissione team", description = "Permette a un team autenticato di creare una nuova sottomissione per l'hackathon.")
    @ApiResponse(responseCode = "200", description = "Sottomissione creata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> createSubmission(
            Authentication authentication,
            @Valid @RequestBody CreateTeamSubmissionRequest createTeamSubmissionRequest) {
        try {
            submissionService.createSubmission(authentication, createTeamSubmissionRequest);
            return ResponseEntity.ok("Sottomissione creata con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Aggiorna una sottomissione esistente.
     */
    @PostMapping("/team/update")
    @Operation(summary = "Aggiorna sottomissione team", description = "Permette di aggiornare una sottomissione già esistente per il team.")
    @ApiResponse(responseCode = "200", description = "Sottomissione aggiornata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> updateSubmission(
            @Valid @RequestBody UpdateTeamSubmissionRequest updateTeamSubmissionRequest) {
        try {
            submissionService.updateSubmission(updateTeamSubmissionRequest);
            return ResponseEntity.ok("Sottomissione aggiornata con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ------------------------------- JUDGE -------------------------------

    /**
     * Valuta una sottomissione di un hackathon.
     */
    @PostMapping("/judge/evaluateHackathonSubmission")
    @Operation(summary = "Valuta sottomissione hackathon", description = "Permette a un giudice di valutare una sottomissione per l'hackathon.")
    @ApiResponse(responseCode = "200", description = "Valutazione completata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> evaluateHackathonSubmission(
            @Valid @RequestBody HackathonSubmissionEvaluationRequest hackathonSubmissionEvaluationRequest) {
        try {
            submissionService.evaluateHackathonSubmission(hackathonSubmissionEvaluationRequest);
            return ResponseEntity.ok("Valutazione completata con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Aggiorna la valutazione di una sottomissione.
     */
    @PostMapping("/judge/updateEvaluation")
    @Operation(summary = "Aggiorna valutazione sottomissione", description = "Permette di aggiornare la valutazione già inserita per una sottomissione.")
    @ApiResponse(responseCode = "200", description = "Valutazione aggiornata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<String> updateEvaluation(
            @Valid @RequestBody HackathonSubmissionEvaluationRequest req) {
        try {
            submissionService.updateHackathonSubmissionEvaluation(req);
            return ResponseEntity.ok("Valutazione aggiornata con successo");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // ------------------------------- STAFF ASSEGNATO
    // -------------------------------

    /**
     * Recupera le sottomissioni assegnate allo staff autenticato.
     */
    @GetMapping("/staff/assigned")
    @Operation(summary = "Sottomissioni assegnate allo staff", description = "Restituisce tutte le sottomissioni attualmente assegnate allo staff autenticato.")
    @ApiResponse(responseCode = "200", description = "Sottomissioni recuperate con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta")
    public ResponseEntity<List<Submission>> getAssignedSubmissions(Authentication authentication) {
        try {
            return ResponseEntity.ok(
                    submissionService.getSubmissionsByStaffMember(authentication.getName()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}