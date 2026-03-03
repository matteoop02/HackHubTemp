package unicam.ids.HackHub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.state.invite.InviteStateFactory;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inside_invites", indexes = {
        @Index(name = "idx_recipient_user", columnList = "recipient_user_id"),
        @Index(name = "idx_team_id", columnList = "team_id"),
        @Index(name = "idx_status", columnList = "status")
})
public class InviteInsidePlatform implements Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "senderUserId", nullable = false)
    private User senderUser;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "recipientUserId", nullable = false)
    private User recipientUser;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "teamId", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "RoleId", nullable = false)
    private UserRole proposedRole;

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