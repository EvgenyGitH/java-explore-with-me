package ru.practicum.statsclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@Data
@AllArgsConstructor
@Validated
public class StatsClientController {
    private final StatsClient statsClient;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> hit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("save Hit");
        return statsClient.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("get Hit's statistic");
        return statsClient.getStats(start, end, uris, unique);
    }

}
