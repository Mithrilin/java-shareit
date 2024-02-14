package ru.practicum.shareit.item.dto;

import lombok.Data;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
public class ItemDto {
    private Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final User owner;
    private final ItemRequest request;
}
