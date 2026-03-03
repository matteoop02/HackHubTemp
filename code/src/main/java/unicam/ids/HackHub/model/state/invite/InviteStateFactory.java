package unicam.ids.HackHub.model.state.invite;

import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.model.Invite;

public class InviteStateFactory {
    public static InviteStateBehavior from(InviteState state) {
        return switch (state) {
            case PENDING -> new PendingInviteState();
            case ACCEPTED -> new AcceptedInviteState();
            case REJECTED -> new RejectedInviteState();
            case EXPIRED -> new ExpiredInviteState();
            case CANCELLED -> new CancelledInviteState();
        };
    }
}

class DefaultInviteState implements InviteStateBehavior {
    private final InviteState state;

    public DefaultInviteState(InviteState state) {
        this.state = state;
    }

    @Override
    public void send(Invite invite) {
        throw new IllegalStateException("Impossibile inviare un invito in stato " + state);
    }

    @Override
    public void accept(Invite invite) {
        throw new IllegalStateException("Impossibile accettare un invito in stato " + state);
    }

    @Override
    public void reject(Invite invite) {
        throw new IllegalStateException("Impossibile rifiutare un invito in stato " + state);
    }

    @Override
    public void cancel(Invite invite) {
        throw new IllegalStateException("Impossibile cancellare un invito in stato " + state);
    }
}
