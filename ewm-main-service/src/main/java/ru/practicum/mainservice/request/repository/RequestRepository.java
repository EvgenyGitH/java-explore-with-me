package ru.practicum.mainservice.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainservice.request.model.ParticipationRequest;
import ru.practicum.mainservice.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByRequesterIdAndEventId(Long requesterId, Long eventId);

    Long countRequestByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);


}
