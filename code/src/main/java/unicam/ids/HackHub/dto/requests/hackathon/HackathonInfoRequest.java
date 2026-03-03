package unicam.ids.HackHub.dto.requests.hackathon;

import jakarta.validation.constraints.NotNull;

public record HackathonInfoRequest(
                @NotNull Long id,
                String name) {
}
