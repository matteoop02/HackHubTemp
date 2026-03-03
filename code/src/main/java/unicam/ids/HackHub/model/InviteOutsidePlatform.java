package unicam.ids.HackHub.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.state.invite.InviteStateFactory;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "outside_invites", indexes = {
        @Index(name = "idx_invite_token", columnList = "invite_token"),
        @Index(name = "idx_recipient_email", columnList = "recipient_email")
})
public class InviteOutsidePlatform implements Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "senderUserId", nullable = false)
    private User senderUser;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "invite_token", nullable = false, unique = true, length = 64)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteState status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 1000)
    private String message;

    @Override
    public void setStatus(InviteState status) {
        this.status = status;
    }

    @Override
    public void send() {
        InviteStateFactory.from(this.status).send(this);
    }

    @Override
    public void accept() {
        InviteStateFactory.from(this.status).accept(this);
    }

    @Override
    public void reject() {
        InviteStateFactory.from(this.status).reject(this);
    }

    @Override
    public void cancel() {
        InviteStateFactory.from(this.status).cancel(this);
    }

    @Override
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) && status == InviteState.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}