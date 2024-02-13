package ru.practicum.shareit.request;

import lombok.Data;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class ItemRequest {
    public Long id;
    private final String description;
    private final User requestor;
    public final LocalDateTime created;
}
