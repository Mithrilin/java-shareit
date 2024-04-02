package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@ControllerAdvice("ru.practicum.shareit")
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({NotValidException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(final RuntimeException e) {
        log.error("Получен статус 400 BAD REQUEST. {}", e.getMessage(), e);
        return Map.of("error", e.getMessage());
    }
}
