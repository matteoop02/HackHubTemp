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
import org.springframework.web.bind.annotation.*;
import unicam.ids.HackHub.dto.requests.payment.VerifyPaymentRequest;
import unicam.ids.HackHub.dto.responses.PaymentStatusResponse;
import unicam.ids.HackHub.service.HackathonService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagamenti", description = "Gestione dei pagamenti relativi agli hackathon")
public class PaymentController {

        private final HackathonService hackathonService;

        @GetMapping("/organizzatore/status")
        @Operation(summary = "Verifica stato pagamento hackathon", description = "Permette all'organizzatore di verificare se la quota di creazione dell'hackathon è stata pagata correttamente.", requestBody = @RequestBody(required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio verifica pagamento", value = """
                        {
                          "hackathonName": "HackathonInnovazione2026"
                        }
                        """))))
        @ApiResponse(responseCode = "200", description = "Stato del pagamento recuperato con successo")
        @ApiResponse(responseCode = "400", description = "Richiesta non valida o hackathon non trovato")
        public ResponseEntity<PaymentStatusResponse> verifyPricePayment(
                        @Valid @RequestBody VerifyPaymentRequest verifyPaymentRequest) {

                return ResponseEntity.ok(
                                hackathonService.verifyHackathonPricePayment(
                                                verifyPaymentRequest.hackathonName()));
        }
}