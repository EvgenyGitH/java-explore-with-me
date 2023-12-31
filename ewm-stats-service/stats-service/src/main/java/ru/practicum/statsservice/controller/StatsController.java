package ru.practicum.statsservice.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.EndpointHitDto;
import ru.practicum.statsdto.ViewStats;
import ru.practicum.statsservice.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Data
@Validated
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto hit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("save Hit");
        return statsService.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("get Hit's statistic");

        return statsService.getStats(start, end, uris, unique);
    }

}
