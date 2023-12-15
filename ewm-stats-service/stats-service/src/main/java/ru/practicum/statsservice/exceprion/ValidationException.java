package ru.practicum.statsservice.exceprion;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
