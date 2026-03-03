package unicam.ids.HackHub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.report.ReportToOrganizerRequest;
import unicam.ids.HackHub.model.Report;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.ReportRepository;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserService userService;
    private final TeamService teamService;
    private final EmailService emailService;

    public void reportRequest(Authentication authentication, ReportToOrganizerRequest request) {
        User mentor = userService.findUserByUsername(authentication.getName());
        User organizer = userService.findUserByUsername(request.organizerUsername());
        Team team = teamService.findByName(request.teamName());

        if (team.getHackathon() == null)
            throw new IllegalArgumentException("Il team non è iscritto a nessun hackathon!");

        if (team.getHackathon().getRegulation().isEmpty())
            throw new IllegalArgumentException("Regolamento hackathon non specificato!");

        // Costruisco il report di violazione
        Report report = Report.builder()
                .description(request.description())
                .hackathon(team.getHackathon())
                .organizer(organizer)
                .team(team)
                .mentor(mentor)
                .type(request.reportType())
                .build();

        // Invio email di notifica all'organizzatore
        emailService.sendEmail(
                organizer.getEmail(),
                mentor.getEmail(),
                report.getType() + " Report",
                report.getType() + " Report" +
                        " - Hackathon: " + team.getHackathon().getName() +
                        " - Team: " + team.getName() +
                        " - Mentore: " + mentor.getName());

        save(report);
    }

    public void save(Report report) {
        reportRepository.save(report);
    }
}
