package unicam.ids.HackHub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
    boolean existsByName(String name);

    @Query("SELECT h FROM Hackathon h WHERE LOWER(h.name) = LOWER(:name)")
    Optional<Hackathon> findHackathonByName(@Param("name") String name);

    List<Hackathon> findAllByIsPublic(Boolean isPublic);

    Optional<Hackathon> findHackathonByNameAndIsPublic(String hackathonName, boolean isPublic);

    List<Hackathon> findHackathonByOrganizer(User organizer);

    List<Hackathon> findHackathonByJudge(User judge);

    List<Hackathon> findHackathonByMentors(User user);

    List<Hackathon> findByStateAndStartDateLessThanEqual(HackathonState hackathonState, LocalDateTime now);

    List<Hackathon> findByStateAndEndDateLessThanEqual(HackathonState hackathonState, LocalDateTime now);

    Optional<Hackathon> findHackathonByIdAndIsPublic(Long id, boolean isPublic);
}
