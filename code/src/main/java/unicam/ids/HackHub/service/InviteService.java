package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import unicam.ids.HackHub.dto.requests.invite.*;
import unicam.ids.HackHub.enums.InviteState;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.model.*;
import unicam.ids.HackHub.factory.InviteFactory;
import unicam.ids.HackHub.repository.OutsideInviteRepository;
import unicam.ids.HackHub.repository.InsideInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    private final InviteFactory inviteFactory;
    private final OutsideInviteRepository outsideInviteRepository;
    private final InsideInviteRepository insideInviteRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final TeamService teamService;
    private final UserService userService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public InviteOutsidePlatform inviteOutsideUser(Authentication authentication,
            OutsideInviteRequest outsideInviteRequest) {
        if (existsBySenderUsernameAndRecipientEmailAndStatus(
                userService.findUserByUsername(authentication.getName()),
                outsideInviteRequest.recipientEmail(),
                InviteState.PENDING))
            throw new IllegalStateException("Esiste già un invito pendente per " + outsideInviteRequest.recipientEmail()
                    + "da parte di " + authentication.getName());

        User senderUser = userService.findUserByUsername(authentication.getName());
        InviteOutsidePlatform invite = inviteFactory.createOutsideInvite(
                senderUser, outsideInviteRequest.recipientEmail(), outsideInviteRequest.message());

        invite.send();
        InviteOutsidePlatform saved = outsideInviteRepository.save(invite);

        emailService.sendEmail(outsideInviteRequest.recipientEmail(), senderUser.getEmail(), "Invito da HackHub",
                outsideInviteRequest.message());

        return saved;
    }

    @Transactional
    public void acceptInviteAndRegisterUser(RegisterFromInviteRequest request) {

        InviteOutsidePlatform invite = findInviteByToken(request.token());

        if (invite.isExpired())
            throw new IllegalStateException("L'invito è scaduto");

        if (invite.getStatus() != InviteState.PENDING)
            throw new IllegalStateException("Invito non più valido");

        if (userService.existsUserByUsername(request.username()))
            throw new IllegalArgumentException("Username già esistente: " + request.username());

        try {
            userService.findUserByEmail(invite.getRecipientEmail());
            throw new IllegalArgumentException("Email già registrata: " + invite.getRecipientEmail());
        } catch (Exception ignored) {
        }

        // Delegate user registration to UserService
        userService.registerUserFromInvite(request, invite.getRecipientEmail());

        invite.accept();
        outsideInviteRepository.save(invite);
    }

    @Transactional
    public void rejectOutsideInvite(RejectOutsideInviteRequest rejectOutsideInviteRequest) {
        InviteOutsidePlatform invite = findInviteByToken(rejectOutsideInviteRequest.token());

        invite.reject();
        outsideInviteRepository.save(invite);
    }

    // ------------------------------- INSIDE INVITE MANAGE
    // -------------------------------

    @Transactional
    public InviteInsidePlatform inviteUserToTeam(InsideInviteRequest insideInviteRequest) {
        // Carico gli utenti
        User senderUser = userService.findUserByUsername(insideInviteRequest.senderUsername());
        User recipientUser = userService.findUserByUsername(insideInviteRequest.recipientUsername());

        // Verifica che non ci sia già un invito pendente per un dato utente da un dato
        // team
        if (existsByRecipientUserAndTeamAndStatus(recipientUser, senderUser.getTeam(), InviteState.PENDING))
            throw new IllegalStateException("Esiste già un invito pendente per questo utente");

        // Carico il ruolo
        UserRole proposedRole = userRoleService.findUserRoleById(insideInviteRequest.proposedRoleId());

        InviteInsidePlatform invite = inviteFactory.createTeamInvite(senderUser, recipientUser, proposedRole,
                insideInviteRequest.message());

        invite.send();
        InviteInsidePlatform saved = insideInviteRepository.save(invite);

        notificationService.sendTeamInviteNotification(saved);
        return saved;
    }

    @Transactional
    public void acceptTeamInvite(Authentication authentication, AcceptTeamInvite acceptTeamInvite) {
        InviteInsidePlatform invite = findInviteById(acceptTeamInvite.inviteId());

        if (!invite.getRecipientUser().getUsername().equals(authentication.getName())) {
            throw new IllegalArgumentException("Non sei autorizzato ad accettare questo invito");
        }

        invite.accept();
        insideInviteRepository.save(invite);

        // Aggiungi l'utente al team
        teamService.addMemberToTeam(invite.getTeam(), userService.findUserByUsername(authentication.getName()),
                invite.getProposedRole());
        notificationService.notifyInviteAccepted(invite);
    }

    @Transactional
    public void rejectTeamInvite(Authentication authentication, Long inviteId) {
        InviteInsidePlatform invite = findInviteById(inviteId);

        if (!invite.getRecipientUser().getUsername().equals(authentication.getName()))
            throw new IllegalArgumentException("Non sei autorizzato a rifiutare questo invito");

        invite.reject();
        insideInviteRepository.save(invite);
        notificationService.notifyInviteRejected(invite);
    }

    // ------------------------------- FIND INVITE -------------------------------

    @Transactional(readOnly = true)
    public List<InviteInsidePlatform> findPendingInvitesForUser(Authentication authentication) {
        return insideInviteRepository.findByRecipientUserAndStatus(
                userService.findUserByUsername(authentication.getName()), InviteState.PENDING);
    }

    @Transactional(readOnly = true)
    public List<InviteInsidePlatform> findTeamInvites(Authentication authentication) {
        Team team = teamService
                .findByName(userService.findUserByUsername(authentication.getName()).getTeam().getName());
        return insideInviteRepository.findByTeamAndStatus(team, InviteState.PENDING);
    }

    public List<Invite> findEmailInvites(String email) {
        // inside invites
        List<InviteInsidePlatform> inside = insideInviteRepository
                .findByRecipientUserAndStatus(userService.findUserByEmail(email), InviteState.PENDING);

        // outside invites
        List<InviteOutsidePlatform> outside = outsideInviteRepository
                .findByRecipientEmailAndStatus(email, InviteState.PENDING);

        List<Invite> allInvites = new ArrayList<>();
        allInvites.addAll(inside);
        allInvites.addAll(outside);

        return allInvites;
    }

    // ------------------------------- UTILITY -------------------------------

    @Transactional
    public InviteInsidePlatform findInviteById(Long inviteId) {
        return insideInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invito non trovato"));
    }

    @Transactional
    public InviteOutsidePlatform findInviteByToken(String token) {
        return outsideInviteRepository.findByInviteToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invito non trovato"));
    }

    @Transactional
    public boolean existsByRecipientUserAndTeamAndStatus(User recipientUser, Team team, InviteState inviteStatus) {
        return insideInviteRepository.existsByRecipientUserAndTeamAndStatus(recipientUser, team, inviteStatus);
    }

    @Transactional
    public boolean existsBySenderUsernameAndRecipientEmailAndStatus(User senderUser, String recipientEmail,
            InviteState inviteStatus) {
        return outsideInviteRepository.existsBySenderUserAndRecipientEmailAndStatus(senderUser, recipientEmail,
                inviteStatus);
    }
}