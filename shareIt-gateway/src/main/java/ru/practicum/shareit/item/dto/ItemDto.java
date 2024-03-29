package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class ItemDto {
    @Positive
    private Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private final Long requestId;
}
