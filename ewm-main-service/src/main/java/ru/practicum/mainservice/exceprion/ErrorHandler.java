package ru.practicum.mainservice.exceprion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException exception) {
        log.error(exception.getMessage());
        return new ApiError(
                HttpStatus.NOT_FOUND.toString(),
                "The required object was not found.",
                exception.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerValidationException(ValidationException exception) {
        log.error(exception.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST.toString(),
                "Incorrectly made request.",
                exception.getMessage(),
                LocalDateTime.now());
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataConflictException(DataConflictException exception) {
        log.error(exception.getMessage());
        return new ApiError(
                HttpStatus.CONFLICT.toString(),
                "Integrity constraint has been violated.",
                exception.getMessage(),
                LocalDateTime.now());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handlerForbiddenOperationException(ForbiddenOperationException exception) {
        log.error(exception.getMessage());
        return new ApiError(
                HttpStatus.FORBIDDEN.toString(),
                "For the requested operation the conditions are not met.",
                exception.getMessage(),
                LocalDateTime.now());
    }


}
