package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final ItemDto itemDto;
    private final UserDto bookerDto;
    private BookingStatus status;
}
