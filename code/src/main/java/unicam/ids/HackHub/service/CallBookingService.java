package unicam.ids.HackHub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.call.CallBookingRequest;
import unicam.ids.HackHub.dto.requests.call.CancelCallRequest;
import unicam.ids.HackHub.dto.requests.call.MentorCallsRequest;
import unicam.ids.HackHub.dto.requests.call.TeamCallsRequest;
import unicam.ids.HackHub.enums.CallState;
import unicam.ids.HackHub.model.CallBooking;
import unicam.ids.HackHub.model.Team;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.CallBookingRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallBookingService {

    private final CallBookingRepository callBookingRepository;
    private final TeamService teamService;
    private final UserService userService;

    public List<CallBooking> getBookingsForMentor(MentorCallsRequest mentorCallsRequest) {
        return callBookingRepository.findByMentorAndStatus(
                userService.findUserByUsername(mentorCallsRequest.mentorUsername()), CallState.PENDING);
    }

    public List<CallBooking> getBookingsForTeam(TeamCallsRequest teamCallsRequest) {
        return callBookingRepository.findByTeamAndStatus(teamService.findByName(teamCallsRequest.teamName()),
                CallState.PENDING);
    }

    @Transactional
    public CallBooking bookCall(Authentication authentication, CallBookingRequest callBookingRequest) {
        User leader = userService.findUserByUsername(authentication.getName());
        Team team = teamService.findByName(leader.getTeam().getName());

        if (!team.getTeamLeader().equals(leader))
            throw new IllegalArgumentException("Solo il leader del team può prenotare una call.");

        User mentor = userService.findUserByUsername(callBookingRequest.mentorUsername());

        CallBooking booking = CallBooking.builder()
                .team(team)
                .mentor(mentor)
                .startTime(callBookingRequest.startTime())
                .endTime(callBookingRequest.endTime())
                .topic(callBookingRequest.topic())
                .status(CallState.PENDING)
                .build();

        return save(booking);
    }

    @Transactional
    public CallBooking cancelCall(Authentication authentication, CancelCallRequest cancelCallRequest) {
        User leader = userService.findUserByUsername(authentication.getName());
        User mentor = userService.findUserByUsername(cancelCallRequest.mentorUsername());

        CallBooking callBooking = findByTeamAndMentor(leader.getTeam(), mentor);

        callBooking.setStatus(CallState.CANCELLED);
        return save(callBooking);
    }

    public CallBooking findByTeamAndMentor(Team team, User mentor) {
        return callBookingRepository.findByTeamAndMentor(team, mentor)
                .orElseThrow(
                        () -> new IllegalArgumentException("Nessuna call trovata con questo mentor per il tuo team"));
    }

    public CallBooking save(CallBooking callBooking) {
        return callBookingRepository.save(callBooking);
    }

}
