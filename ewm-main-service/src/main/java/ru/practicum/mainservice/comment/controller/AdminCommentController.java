package ru.practicum.mainservice.comment.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/admin/comments")
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getAllCommentsByAdmin(@RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) Long eventId,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                  @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Request Admin: Get all Comments");
        return commentService.getAllCommentsByAdmin(userId, eventId, rangeStart, rangeEnd, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Request Admin: delete Comment");
        commentService.deleteCommentByAdmin(commentId);
    }
}
