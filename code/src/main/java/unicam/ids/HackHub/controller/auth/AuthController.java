package unicam.ids.HackHub.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import unicam.ids.HackHub.dto.requests.auth.LoginUserRequest;
import unicam.ids.HackHub.dto.requests.auth.RegisterUserRequest;
import unicam.ids.HackHub.dto.responses.AuthResponse;
import unicam.ids.HackHub.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticazione", description = "Gestione della registrazione e login utenti")
public class AuthController {

        private final AuthService authService;

        @PostMapping("/register")
        @SecurityRequirements()
        @Operation(summary = "Registra un nuovo utente", description = "Permette la registrazione di un nuovo utente con username, email, password e ruolo", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dati dell'utente da registrare", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio registrazione", value = """
                        {
                              "name": "matteo",
                              "surname": "fagnani",
                              "username": "matteop",
                              "email": "matteo.fagnani@studenti.unicam.it",
                              "password": "123456789",
                              "dateOfBirth": "2002-03-02",
                              "roleId": 1
                        }
                        """))))
        @ApiResponse(responseCode = "201", description = "Utente registrato con successo")
        @ApiResponse(responseCode = "400", description = "Richiesta non valida o dati mancanti")
        public ResponseEntity<Void> register(@RequestBody @Valid RegisterUserRequest request) {
                authService.register(request);
                return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        @PostMapping("/login")
        @SecurityRequirements()
        @Operation(summary = "Login utente", description = "Permette a un utente registrato di autenticarsi e ricevere un token JWT", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenziali di login dell'utente", required = true, content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio login", value = """
                        {
                          "username": "matteop",
                          "password": "123456789"
                        }
                        """))))
        @ApiResponse(responseCode = "200", description = "Login effettuato con successo, token restituito")
        @ApiResponse(responseCode = "401", description = "Credenziali non valide")
        public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginUserRequest loginRequest) {
                AuthResponse response = authService.login(loginRequest);
                return ResponseEntity.ok(response);
        }
}