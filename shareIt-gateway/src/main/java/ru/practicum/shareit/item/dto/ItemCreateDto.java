package ru.practicum.shareit.item.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemCreateDto {
    @NotBlank(message = "Название вещи не может быть пустым.")
    private final String name;
    @NotBlank(message = "Описание вещи не может быть пустым.")
    private final String description;
    @NotNull(message = "Доступность не может быть null")
    private final Boolean available;
    private final Long requestId;
}
