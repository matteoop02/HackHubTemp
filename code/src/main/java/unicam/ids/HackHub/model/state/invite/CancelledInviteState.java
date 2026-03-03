package unicam.ids.HackHub.model.state.invite;

import unicam.ids.HackHub.model.Invite;

public class CancelledInviteState implements InviteStateBehavior {
    @Override
    public void send(Invite invite) {
        throw new IllegalStateException("L'invito è stato annullato");
    }

    @Override
    public void accept(Invite invite) {
        throw new IllegalStateException("L'invito è stato annullato");
    }

    @Override
    public void reject(Invite invite) {
        throw new IllegalStateException("L'invito è stato annullato");
    }

    @Override
    public void cancel(Invite invite) {
        throw new IllegalStateException("L'invito è già stato annullato");
    }
}
