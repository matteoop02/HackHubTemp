package unicam.ids.HackHub.model.state.invite;

import unicam.ids.HackHub.model.Invite;

public interface InviteStateBehavior {
    void send(Invite invite);

    void accept(Invite invite);

    void reject(Invite invite);

    void cancel(Invite invite);
}
