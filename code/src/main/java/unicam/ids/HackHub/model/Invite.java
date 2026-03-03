package unicam.ids.HackHub.model;

import java.time.LocalDateTime;
import unicam.ids.HackHub.enums.InviteState;

public interface Invite {
    Long getId();

    User getSenderUser();

    InviteState getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getExpiresAt();

    void setStatus(InviteState status);

    void send();

    void accept();

    void reject();

    void cancel();

    boolean isExpired();
}