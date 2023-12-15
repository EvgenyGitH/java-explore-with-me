package ru.practicum.mainservice.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.category.model.Category;
import ru.practicum.mainservice.category.repository.CategoryRepository;
import ru.practicum.mainservice.event.dto.*;
import ru.practicum.mainservice.event.mapper.EventMapper;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.model.Location;
import ru.practicum.mainservice.event.model.State;
import ru.practicum.mainservice.event.repository.EventRepository;
import ru.practicum.mainservice.event.repository.LocationRepository;
import ru.practicum.mainservice.exceprion.DataConflictException;
import ru.practicum.mainservice.exceprion.NotFoundException;
import ru.practicum.mainservice.exceprion.ValidationException;
import ru.practicum.mainservice.request.dto.ParticipationRequestDto;
import ru.practicum.mainservice.request.mapper.RequestMapper;
import ru.practicum.mainservice.request.model.ParticipationRequest;
import ru.practicum.mainservice.request.model.RequestStatus;
import ru.practicum.mainservice.request.repository.RequestRepository;
import ru.practicum.mainservice.user.model.User;
import ru.practicum.mainservice.user.repository.UserRepository;
import ru.practicum.statsclient.StatsClient;
import ru.practicum.statsdto.EndpointHitDto;
import ru.practicum.statsdto.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImp implements EventService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_END = "2050-01-01 00:00:00";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;


    // -- Private --
    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<EventShortDto> events = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
        return events;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + newEventDto.getEventDate());
        }
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " is not found."));
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() ->
                new NotFoundException("Category with id=" + newEventDto.getCategory() + " is not found."));
        Location location = locationRepository.save(newEventDto.getLocation());
        Event event = EventMapper.toEvent(newEventDto, category, user, location);
        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event);

    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));
        if (event.getState().equals(State.PUBLISHED)) {
            throw new DataConflictException("Event must not be published.");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserRequest.getCategory()).orElseThrow(() ->
                    new NotFoundException("Category with id=" + updateEventUserRequest.getCategory() + " is not found."));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "EventDate must be 2 hour before event.");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(updateEventUserRequest.getLocation());
            updateEventUserRequest.getLocation().setId(event.getLocation().getId());
            locationRepository.save(updateEventUserRequest.getLocation());
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    break;
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        event = eventRepository.save(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsToEvent(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Only Initiator can get Event's Request.");
        }
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatusToEvent(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));

        List<Long> requestIds = request.getRequestIds();
        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(requestIds);
        switch (request.getStatus()) {
            case REJECTED:
                for (ParticipationRequest requestFromList : requests) {
                    if (requestFromList.getStatus().equals(RequestStatus.CONFIRMED)) {
                        throw new DataConflictException("Cannot reject confirmed request.");
                    }
                    requestFromList.setStatus(RequestStatus.REJECTED);
                }
                break;
            case CONFIRMED:
                for (ParticipationRequest requestFromList : requests) {
                    if (!requestFromList.getStatus().equals(RequestStatus.PENDING)) {
                        throw new DataConflictException("Status cannot be changed. Status: " + requestFromList.getStatus());
                    }
                    if (!requestFromList.getEvent().getRequestModeration() || requestFromList.getEvent().getParticipantLimit() == 0) {
                        requestFromList.setStatus(RequestStatus.CONFIRMED);
                    }
                    if (requestFromList.getEvent().getParticipantLimit() != 0 && requestRepository.countRequestByEventIdAndStatus(requestFromList.getEvent().getId(), RequestStatus.CONFIRMED) >= requestFromList.getEvent().getParticipantLimit()) {
                        requestFromList.setStatus(RequestStatus.REJECTED);
                        throw new DataConflictException("Confirmed participants = Participants Limit.");
                    } else {
                        requestFromList.setStatus(RequestStatus.CONFIRMED);
                    }
                }
                break;
        }
        requestRepository.saveAll(requests);
        List<ParticipationRequestDto> confirmed = requests.stream()
                .filter(req -> req.getStatus().equals(RequestStatus.CONFIRMED))
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
        List<ParticipationRequestDto> rejected = requests.stream()
                .filter(req -> req.getStatus().equals(RequestStatus.REJECTED))
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);
        return result;
    }

    // -- Admin --
    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> usersIds, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.parse(DEFAULT_END, formatter);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Not correct dates");
        }
        if (usersIds != null && usersIds.isEmpty()) {
            usersIds = null;
        }
        if (states != null && states.isEmpty()) {
            states = null;
        }
        if (categories != null && categories.isEmpty()) {
            categories = null;
        }
        List<Event> events = eventRepository.findAllByAdmin(usersIds, states, categories, rangeStart, rangeEnd, pageable);
        Map<String, Long> stats = getViews(events);
        for (Event event : events) {
            event.setConfirmedRequests(requestRepository.countRequestByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED));
            Long views = stats.getOrDefault("/events/" + event.getId(), 0L);
            event.setViews(views);
        }
        List<EventFullDto> eventFullDtoList = events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
        return eventFullDtoList;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));
        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(
                        "EventDate must be 2 hour before event. Value: " + event.getEventDate());
            }
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT:
                    if (!event.getState().equals(State.PENDING)) {
                        throw new DataConflictException(
                                "Cannot publish the event because it's not in the right state: PENDING");
                    }
                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new DataConflictException(
                                "Changes must be made 1 hour before event " + event.getEventDate());
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (!event.getState().equals(State.PENDING)) {
                        throw new DataConflictException(
                                "Cannot cancel the event because it's not in the right state: PENDING");
                    }
                    event.setState(State.CANCELED);
                    break;
            }
        }

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminRequest.getCategory()).orElseThrow(() ->
                    new NotFoundException("Category with id=" + updateEventAdminRequest.getCategory() + " is not found."));
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(updateEventAdminRequest.getLocation());
            updateEventAdminRequest.getLocation().setId(event.getLocation().getId());
            locationRepository.save(updateEventAdminRequest.getLocation());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));

    }

    // -- Public --
    @Override
    public List<EventShortDto> getEventsByPublicUser(String text, List<Long> categories, Boolean paid,
                                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable, String sort, int from, int size,
                                                     HttpServletRequest servletRequest) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("emw-main-service")
                .uri("/events")
                .ip(servletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveHit(endpointHitDto);

        Pageable pageable = PageRequest.of(from / size, size);

        if (categories != null && categories.isEmpty()) {
            categories = null;
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.parse(DEFAULT_END, formatter);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Not correct dates");
        }

        List<Event> events = eventRepository.findAllByPublic(text, categories, paid, rangeStart, rangeEnd, pageable);
        Map<String, Long> stats = getViews(events);
        for (Event event : events) {
            event.setConfirmedRequests(requestRepository.countRequestByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED));
            Long views = stats.getOrDefault("/events/" + event.getId(), 0L);
            event.setViews(views);
        }
        if (onlyAvailable) {
            events = events.stream().filter(event -> event.getParticipantLimit() == 0 || event.getParticipantLimit() < event.getConfirmedRequests())
                    .collect(Collectors.toList());
        }
        if (sort != null) {
            switch (sort) {
                case "VIEWS":
                    return events.stream()
                            .map(EventMapper::toEventShortDto)
                            .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                            .collect(Collectors.toList());
                case "EVENT_DATE":
                    return events.stream()
                            .map(EventMapper::toEventShortDto)
                            .sorted(Comparator.comparing(EventShortDto::getEventDate).reversed())
                            .collect(Collectors.toList());
            }
        }
        return events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventsByIdPublicUser(Long eventId, HttpServletRequest servletRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " is not found.");
        }

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("emw-main-service")
                .uri("/events/" + eventId)
                .ip(servletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveHit(endpointHitDto);

        List<Event> events = new ArrayList<>();
        events.add(event);
        Map<String, Long> stats = getViews(events);
        event.setConfirmedRequests(requestRepository.countRequestByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED));
        Long views = stats.getOrDefault("/events/" + event.getId(), 0L);
        event.setViews(views);
        return EventMapper.toEventFullDto(event);
    }

    private Map<String, Long> getViews(List<Event> events) {
        LocalDateTime start = LocalDateTime.parse("1980-01-01 00:00:00", formatter);
        LocalDateTime end = LocalDateTime.now();

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        ResponseEntity<Object> response = statsClient.getStats(start, end, uris, true);
        Map<String, Long> result = new HashMap<>();
        if (response.getStatusCode() == HttpStatus.OK) {
            List<ViewStats> stats = objectMapper.convertValue(response.getBody(), new TypeReference<List<ViewStats>>() {
            });
            for (ViewStats viewStats : stats) {
                result.put(viewStats.getUri(), viewStats.getHits());
            }
        }
        return result;
    }

}
