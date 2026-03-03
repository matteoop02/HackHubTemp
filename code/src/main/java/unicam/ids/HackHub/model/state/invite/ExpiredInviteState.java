package unicam.ids.HackHub.model.state.invite;

import unicam.ids.HackHub.model.Invite;

public class ExpiredInviteState implements InviteStateBehavior {
    @Override
    public void send(Invite invite) {
        throw new IllegalStateException("L'invito è scaduto");
    }

    @Override
    public void accept(Invite invite) {
        throw new IllegalStateException("L'invito è scaduto");
    }

    @Override
    public void reject(Invite invite) {
        throw new IllegalStateException("L'invito è scaduto");
    }

    @Override
    public void cancel(Invite invite) {
        throw new IllegalStateException("L'invito è scaduto");
    }
}
