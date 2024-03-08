package ru.practicum.shareit.exception;

public class NotOwnerOrBookerException extends RuntimeException {
    public NotOwnerOrBookerException(String message) {
        super(message);
    }
}
