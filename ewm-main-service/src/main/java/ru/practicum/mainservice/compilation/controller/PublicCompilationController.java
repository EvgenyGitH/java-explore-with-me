package ru.practicum.mainservice.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.compilation.dto.CompilationDto;
import ru.practicum.mainservice.compilation.service.CompilationService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilationsByAnyUser(@RequestParam(required = false) Boolean pinned,
                                                         @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                                         @RequestParam(required = false, defaultValue = "10") @Positive int size) {
        log.info("request Public: get Compilations");
        return compilationService.getCompilationsPublic(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationByIdAnyUser(@PathVariable Long compId) {
        log.info("request Public: get Compilations by Id");
        return compilationService.getCompilationByIdPublic(compId);
    }

}
