package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice("ru.practicum.shareit")
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleNotValid(final DataAccessException e) {
        log.error("Получен статус 409 CONFLICT. {}", e.getMessage(), e);
        return Map.of("errorMessage", Objects.requireNonNull(e.getMessage()));
    }

    // Отлавливаем все NotFoundException
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        log.error("Получен статус 404 Not found. {}", e.getMessage(), e);
        return Map.of("errorMessage", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAllException(final ValidationException e) {
        log.error("Получен статус 400 BAD REQUEST. {}", e.getMessage(), e);
        return Map.of("errorMessage", e.getMessage());
    }
}
