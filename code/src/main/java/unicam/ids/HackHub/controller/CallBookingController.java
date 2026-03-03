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
import unicam.ids.HackHub.dto.requests.call.CallBookingRequest;
import unicam.ids.HackHub.dto.requests.call.CancelCallRequest;
import unicam.ids.HackHub.dto.requests.call.MentorCallsRequest;
import unicam.ids.HackHub.dto.requests.call.TeamCallsRequest;
import unicam.ids.HackHub.model.CallBooking;
import unicam.ids.HackHub.service.CallBookingService;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
@Tag(name = "Prenotazioni Call", description = "Permette di prenotare call con i mentori sia del team che esterni")
public class CallBookingController {

        private final CallBookingService callBookingService;

        @GetMapping("/staff/callForMentor")
        @Operation(summary = "Get di una lista di call", description = """
                            Permette ad un mentore di vedere la sua lista di call.
                        """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per la get delle call", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio get calls", value = """
                            {
                              "mentorUsername": "Username"
                            }
                        """))))
        @ApiResponse(responseCode = "200", description = "Lista call per mentore ritornata con successo!")
        @ApiResponse(responseCode = "400", description = "Errore durante la get delle call")
        public ResponseEntity<List<CallBooking>> getCallForMentor(
                        @RequestBody @Valid MentorCallsRequest mentorCallsRequest) {
                List<CallBooking> calls = callBookingService.getBookingsForMentor(mentorCallsRequest);
                return ResponseEntity.ok(calls);
        }

        @GetMapping("/team/callForTeam")
        @Operation(summary = "Get di una lista di call", description = """
                            Permette ad un team di vedere la sua lista di call.
                        """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per la get delle call", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio get calls", value = """
                            {
                              "teamName": "Team1"
                            }
                        """))))
        @ApiResponse(responseCode = "200", description = "Lista call per team ritornata con successo!")
        @ApiResponse(responseCode = "400", description = "Errore durante la get delle call")
        public ResponseEntity<List<CallBooking>> getCallForTeam(@RequestBody @Valid TeamCallsRequest teamCallsRequest) {
                List<CallBooking> calls = callBookingService.getBookingsForTeam(teamCallsRequest);
                return ResponseEntity.ok(calls);
        }

        @PostMapping("/leaderDelTeam/book")
        @Operation(summary = "Prenotazione di una call", description = """
                            Permette ad un team di prenotare una call con un mentore.
                        """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per la prenotazione della call", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio prenotazione call", value = """
                            {
                              "teamName": "Team1"
                              "mentorUsername": "Mentoree"
                              "startTime": "2026-04-08T12:00:0"
                              "endTime": "2026-04-08T12:59:59"
                              "topic": "Qualcosa"
                            }
                        """))))
        @ApiResponse(responseCode = "200", description = "Call prenotata con successo!")
        @ApiResponse(responseCode = "400", description = "Errore durante la prenotazione della call")
        public ResponseEntity<CallBooking> bookCall(Authentication authentication,
                        @RequestBody @Valid CallBookingRequest callBookingRequest) {
                CallBooking booking = callBookingService.bookCall(
                                authentication,
                                callBookingRequest);
                return ResponseEntity.ok(booking);
        }

        @DeleteMapping("/leaderDelTeam/cancel")
        @Operation(summary = "Cancellazione di una call", description = """
                            Permette ad un team di cancellare una call con un mentore.
                        """, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "Dati per la cancellazione della call", content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Esempio cancellazione call", value = """
                            {
                              "mentorUsername": "Mentoree"
                            }
                        """))))
        @ApiResponse(responseCode = "200", description = "Call cancellata con successo!")
        @ApiResponse(responseCode = "400", description = "Errore durante la cancellazione della call")
        public ResponseEntity<CallBooking> cancelCall(Authentication authentication,
                        @RequestBody @Valid CancelCallRequest cancelCallRequest) {
                CallBooking booking = callBookingService.cancelCall(
                                authentication,
                                cancelCallRequest);
                return ResponseEntity.ok(booking);
        }
}
