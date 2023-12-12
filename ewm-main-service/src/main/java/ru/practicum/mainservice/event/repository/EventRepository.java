package ru.practicum.mainservice.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    List<Event> findAllByIdIn(List<Long> ids);

    Boolean existsByCategoryId(Long catId);

    @Query("select e " +
            "from Event e " +
            "where (e.initiator.id in ?1 or ?1 is null) " +
            "and (e.state in ?2 or ?2 is null) " +
            "and (e.category.id in ?3  or ?3 is null) " +
            "and (e.eventDate between ?4 and ?5) "
    )
    List<Event> findAllByAdmin(List<Long> usersIds,
                               List<State> states,
                               List<Long> categories,
                               LocalDateTime rangeStart,
                               LocalDateTime rangeEnd,
                               Pageable pageable);

    @Query("select e " +
            "from Event e " +
            "where (e.state = 'PUBLISHED') " +
            "and ((lower(e.annotation) like lower(concat('%', ?1,'%')) or lower(e.description) like lower(concat('%', ?1,'%'))) or ?1 is null) " +
            "and (e.category.id in ?2 or ?2 is null) " +
            "and (e.paid = ?3 or ?3 is null) " +
            "and (e.eventDate between ?4 and ?5) "
    )
    List<Event> findAllByPublic(String text,
                                List<Long> categories,
                                Boolean paid,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                Pageable pageable);

}
