package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.params.PageRequestParams;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @RequestBody BookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long bookingId,
                                     @RequestParam boolean approved) {
        return bookingService.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") long userId,
                                     @PathVariable long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingByBookerId(@RequestHeader("X-Sharer-User-Id") long bookerId,
                                                    @RequestParam BookingState state,
                                                    @RequestParam int from,
                                                    @RequestParam int size) {
        final String sortBy = "start";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.DESC, sortBy);
        return bookingService.getAllBookingByBookerId(bookerId, state, pageRequestParams);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId,
                                                   @RequestParam BookingState state,
                                                   @RequestParam int from,
                                                   @RequestParam int size) {
        final String sortBy = "start";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.DESC, sortBy);
        return bookingService.getAllBookingByOwnerId(ownerId, state, pageRequestParams);
    }
}
