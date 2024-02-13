package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    public Long id;
    public final LocalDateTime start;
    public final LocalDateTime end;
    public final Item item;
    public final User booker;
    public String status;
}
