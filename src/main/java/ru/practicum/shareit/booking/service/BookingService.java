package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(long userId, BookingDto bookingDto);

    BookingDto approveBooking(long userId, long bookingId, boolean approved);

    BookingDto getBookingById(long userId, long bookingId);

    List<BookingDto> getAllBookingByBookerId(long bookerId, String state);

    List<BookingDto> getAllBookingByOwnerId(long ownerId, String state);
}
