package ru.practicum.mainservice.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.dto.NewCommentDto;
import ru.practicum.mainservice.comment.mapper.CommentMapper;
import ru.practicum.mainservice.comment.model.Comment;
import ru.practicum.mainservice.comment.repository.CommentRepository;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.repository.EventRepository;
import ru.practicum.mainservice.exceprion.DataConflictException;
import ru.practicum.mainservice.exceprion.NotFoundException;
import ru.practicum.mainservice.exceprion.ValidationException;
import ru.practicum.mainservice.user.model.User;
import ru.practicum.mainservice.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImp implements CommentService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_START = "1980-01-01 00:00:00";
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    // -- Private --
    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " is not found."));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event with id=" + eventId + " is not found."));
        Comment comment = CommentMapper.toComment(author, event, newCommentDto);
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " is not found.");
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " is not found."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException("Only the Author can update a comment.");
        }
        comment.setText(newCommentDto.getText());
        commentRepository.save(comment);
        return CommentMapper.toCommentDto(comment);

    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment with id=" + commentId + " is not found."));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException("Only the Author can delete a comment.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getAllUserComments(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not found.");
        }
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    // -- Admin --
    @Override
    public List<CommentDto> getAllCommentsByAdmin(Long userId, Long eventId, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.parse(DEFAULT_START, formatter);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now();
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Not correct dates");
        }
        if (userId != null) {
            if (!userRepository.existsById(userId)) {
                throw new NotFoundException("User with id=" + userId + " is not found.");
            }
        }
        if (eventId != null) {
            if (!eventRepository.existsById(eventId)) {
                throw new NotFoundException("Event with id=" + eventId + " is not found.");
            }
        }
        List<Comment> comments = commentRepository.findAllByAdmin(userId, eventId, rangeStart, rangeEnd, pageable);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=" + commentId + " is not found.");
        }
        commentRepository.deleteById(commentId);
    }

}
