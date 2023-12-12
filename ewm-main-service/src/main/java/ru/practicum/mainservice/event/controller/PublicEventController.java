package ru.practicum.mainservice.event.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.event.dto.EventFullDto;
import ru.practicum.mainservice.event.dto.EventShortDto;
import ru.practicum.mainservice.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Data
@Slf4j
@Validated
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByPublicUser(@RequestParam(required = false) String text,
                                                     @RequestParam(required = false) List<Long> categories,
                                                     @RequestParam(required = false) Boolean paid,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                     @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                                     @RequestParam(required = false) String sort,
                                                     @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                                     @RequestParam(required = false, defaultValue = "10") @Positive int size,
                                                     HttpServletRequest servletRequest) {

        log.info("request Public: Get events by param");
        return eventService.getEventsByPublicUser(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, servletRequest);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventsByIdPublicUser(@PathVariable Long id, HttpServletRequest servletRequest) {
        log.info("request Public: Get events by Id");

        return eventService.getEventsByIdPublicUser(id, servletRequest);
    }

}
