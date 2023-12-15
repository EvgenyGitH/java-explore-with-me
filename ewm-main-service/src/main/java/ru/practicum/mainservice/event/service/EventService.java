package ru.practicum.mainservice.event.service;

import ru.practicum.mainservice.event.dto.*;
import ru.practicum.mainservice.event.model.State;
import ru.practicum.mainservice.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequestsToEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatusToEvent(Long userId, Long eventId, EventRequestStatusUpdateRequest request);

    List<EventFullDto> getEventsByAdmin(List<Long> usersIds, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getEventsByPublicUser(String text, List<Long> categories, Boolean paid,
                                              LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                              Boolean onlyAvailable, String sort, int from, int size,
                                              HttpServletRequest servletRequest);

    EventFullDto getEventsByIdPublicUser(Long eventId, HttpServletRequest servletRequest);

}
