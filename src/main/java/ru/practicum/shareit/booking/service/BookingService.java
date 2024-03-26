package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.params.PageRequestParams;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(long userId, BookingDto bookingDto);

    BookingDto approveBooking(long userId, long bookingId, boolean approved);

    BookingDto getBookingById(long userId, long bookingId);

    List<BookingDto> getAllBookingByBookerId(long bookerId, String state, PageRequestParams pageRequestParams);

    List<BookingDto> getAllBookingByOwnerId(long ownerId, String state, PageRequestParams pageRequestParams);
}
