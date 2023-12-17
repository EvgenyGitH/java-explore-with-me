package ru.practicum.mainservice.comment.service;

import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto createComment(Long eventId, Long authorId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long commentId);

    List<CommentDto> getAllUserComments(Long userId, int from, int size);

    List<CommentDto> getAllCommentsByAdmin(Long userId, Long eventId, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    void deleteCommentByAdmin(Long commentId);

}
