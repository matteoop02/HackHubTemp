package unicam.ids.HackHub.model.state.invite;

import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.Invite;

import java.time.LocalDateTime;

public class PendingInviteState implements InviteStateBehavior {

    @Override
    public void send(Invite invite) {
        // Already pending, maybe already sent
    }

    @Override
    public void accept(Invite invite) {
        if (LocalDateTime.now().isAfter(invite.getExpiresAt())) {
            invite.setStatus(InviteState.EXPIRED);
            throw new IllegalStateException("L'invito è scaduto");
        }
        invite.setStatus(InviteState.ACCEPTED);
    }

    @Override
    public void reject(Invite invite) {
        invite.setStatus(InviteState.REJECTED);
    }

    @Override
    public void cancel(Invite invite) {
        invite.setStatus(InviteState.CANCELLED);
    }
}
