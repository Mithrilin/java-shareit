package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserCreateDto {
    @NotBlank(message = "Имя пользователя не может быть пустым.")
    private final String name;
    @Email
    @NotBlank(message = "Должен быть указан email.")
    private final String email;
}
