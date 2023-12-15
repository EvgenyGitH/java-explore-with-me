package ru.practicum.mainservice.exceprion;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
