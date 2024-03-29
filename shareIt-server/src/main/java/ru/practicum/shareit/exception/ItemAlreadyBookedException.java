package ru.practicum.shareit.exception;

public class ItemAlreadyBookedException extends RuntimeException {
    public ItemAlreadyBookedException(String message) {
        super(message);
    }
}
