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
import unicam.ids.HackHub.dto.requests.report.ReportToOrganizerRequest;
import unicam.ids.HackHub.service.ReportService;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "Gestione delle segnalazioni (violazioni, comportamenti scorretti, richieste ai organizzatori)")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/mentor/reportRequest")
    @Operation(summary = "Invia segnalazione all'organizzatore", description = "Permette a un mentor autenticato di inviare una segnalazione riguardante una richiesta o un comportamento scorretto a un organizzatore.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio segnalazione", value = """
            {
              "hackathonName": "HackathonInnovazione2026",
              "requestId": 15,
              "reason": "Contenuto non conforme al regolamento",
              "description": "La richiesta contiene materiale non appropriato."
            }
            """))))
    @ApiResponse(responseCode = "200", description = "Segnalazione effettuata con successo")
    @ApiResponse(responseCode = "400", description = "Errore nella richiesta o dati non validi")
    public ResponseEntity<String> reportRequest(
            Authentication authentication,
            @Valid @RequestBody ReportToOrganizerRequest reportToOrganizerRequest) {

        try {
            reportService.reportRequest(authentication, reportToOrganizerRequest);
            return ResponseEntity.ok("Segnalazione effettuata con successo.");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}