package ru.practicum.mainservice.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainservice.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    List<Comment> findAllByEventId(Long eventId);
    List<Comment> findAllByEventIdIn(List<Long> eventIds);

    @Query("select com " +
            "from Comment com " +
            "where (com.author.id = ?1 or ?1 is null) " +
            "and (com.event.id = ?2 or ?2 is null) " +
            "and (com.created between ?3 and ?4) "
    )
    List<Comment> findAllByAdmin(Long userId,
                                 Long eventId,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Pageable pageable);


}
