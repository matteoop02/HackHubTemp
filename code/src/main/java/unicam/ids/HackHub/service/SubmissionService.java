package unicam.ids.HackHub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicam.ids.HackHub.dto.requests.submission.CreateTeamSubmissionRequest;
import unicam.ids.HackHub.dto.requests.submission.HackathonSubmissionEvaluationRequest;
import unicam.ids.HackHub.dto.requests.submission.UpdateTeamSubmissionRequest;
import unicam.ids.HackHub.enums.SubmissionState;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import unicam.ids.HackHub.factory.HackathonStateFactory;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.Submission;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.SubmissionRepository;
import unicam.ids.HackHub.model.state.HackathonState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final HackathonService hackathonService;
    private final TeamService teamService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByHackathonName(String name) {
        return submissionRepository.findByHackathonName(name);
    }

    @Transactional(readOnly = true)
    public List<Submission> getSubmissionsByTeamName(String name) {
        return submissionRepository.findByTeamName(name);
    }

    @Transactional(readOnly = true)
    public Submission getSubmissionsByTeamNameAndHackathonNameAndStateIsNot(String name, String hackathonName,
            SubmissionState state) {
        return submissionRepository.findByTeamNameAndHackathonNameAndStateIsNot(name, hackathonName, state)
                .orElseThrow(() -> new ResourceNotFoundException("Submission non trovata"));
    }

    public boolean existsSubmissionByTeamNameAndHackathonName(String teamName, String hackathonName) {
        return submissionRepository.existsSubmissionByTeamNameAndHackathonName(teamName, hackathonName);
    }

    @Transactional
    public void evaluateHackathonSubmission(HackathonSubmissionEvaluationRequest request) {
        Hackathon hackathon = hackathonService.findHackathonByName(request.hackathonName());
        Team team = teamService.findByName(request.teamName());
        Submission submission = getSubmissionsByTeamNameAndHackathonNameAndStateIsNot(team.getName(),
                hackathon.getName(), SubmissionState.VALUTATA);
        HackathonState hackathonState = HackathonStateFactory.from(hackathon.getState());
        hackathonState.evaluateHackathonSubmission(request, submission);
        submissionRepository.save(submission);
    }

    @Transactional
    public void updateHackathonSubmissionEvaluation(HackathonSubmissionEvaluationRequest request) {

        Hackathon hackathon = hackathonService.findHackathonByName(request.hackathonName());
        Team team = teamService.findByName(request.teamName());
        if (LocalDateTime.now().isAfter(hackathon.getEndDate())) {
            throw new IllegalArgumentException("Scadenza per modificare la valutazione superata");
        }
        Submission submission = submissionRepository
                .findByTeamNameAndHackathonName(team.getName(), hackathon.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Submission non trovata"));
        HackathonState hackathonState = HackathonStateFactory.from(hackathon.getState());
        hackathonState.evaluateHackathonSubmission(request, submission);

        submissionRepository.save(submission);
    }

    public void createSubmission(Authentication authentication, CreateTeamSubmissionRequest request) {
        User user = userService.findUserByUsername(authentication.getName());
        if (user.getTeam() == null) {
            throw new IllegalArgumentException("L'utente non appartiene a nessun team");
        }
        Team team = teamService.findByName(user.getTeam().getName());
        if (team.getHackathon() == null) {
            throw new IllegalArgumentException("Il team non è iscritto a nessun hackathon");
        }
        if (team.getTeamLeader() == null ||
                !team.getTeamLeader().getUsername().equals(authentication.getName())) {
            throw new IllegalArgumentException("Solo il leader del team può inviare la sottomissione");
        }
        if (team.getHackathon().getSubscriptionDeadline() == null) {
            throw new IllegalArgumentException("Scadenza sottomissione non impostata per questo hackathon");
        }
        if (java.time.LocalDateTime.now().isAfter(team.getHackathon().getSubscriptionDeadline())) {
            throw new IllegalArgumentException("Scadenza sottomissione superata");
        }
        if (existsSubmissionByTeamNameAndHackathonName(team.getName(), team.getHackathon().getName())) {
            throw new IllegalArgumentException(
                    "Sottomissione già esistente per " + team.getName() + " e " + team.getHackathon().getName());
        }

        HackathonState hackathonState = HackathonStateFactory.from(team.getHackathon().getState());
        Submission submission = hackathonState.createSubmission(request.title(), request.content(), team);
        submissionRepository.save(submission);
    }

    @Transactional
    public void updateSubmission(UpdateTeamSubmissionRequest request) {
        User user = userService.findUserByUsername(request.authentication().getName());
        Team team = teamService.findByName(user.getTeam().getName());

        if (LocalDateTime.now().isAfter(team.getHackathon().getSubscriptionDeadline()))
            throw new IllegalArgumentException("Scadenza sottomissione superata");

        // Cerco la Submission attuale ovvero quella che appartiene all'hackathon
        // attuale
        Submission submission = getSubmissionsByTeamNameAndHackathonNameAndStateIsNot(team.getName(),
                team.getHackathon().getName(), SubmissionState.VALUTATA);

        submission.setTitle(request.title()); // Aggiorno il titolo
        submission.setContent(request.content()); // Aggiorno il contenuto
        submission.setLastEdit(LocalDateTime.now()); // Aggiorno ultima modifica

        submissionRepository.save(submission);
    }

    public List<Submission> getSubmissionsByStaffMember(String staffUsername) {
        User staff = userService.findUserByUsername(staffUsername);
        List<Hackathon> hackathons = hackathonService.getHackathonsByUser(staff);

        List<Submission> submissions = new ArrayList<>();

        if (!hackathons.isEmpty()) {
            for (int i = 0; i < (hackathons.size()); i++) {
                submissions.addAll(getSubmissionsByHackathonName(hackathons.get(i).getName()));
            }
        }

        return submissions;
    }
}
