package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.NotValidException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") long userId,
												@RequestBody @Valid BookingDto bookingDto) {
		log.info("Creating booking {}, userId={}", bookingDto, userId);
		if (bookingDto.getStart() == null
				|| bookingDto.getEnd() == null
				|| bookingDto.getStart().isBefore(LocalDateTime.now())
				|| !bookingDto.getStart().isBefore(bookingDto.getEnd())) {
			log.error("Даты бронирования заданы неверно.");
			throw new NotValidException("Даты бронирования заданы неверно.");
		}
		return bookingClient.addBooking(userId, bookingDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") long userId,
									 			 @PathVariable @Positive long bookingId,
									 			 @RequestParam boolean approved) {
		log.info("Approving booking with id {}", bookingId);
		return bookingClient.approveBooking(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllBookingByBookerId(@RequestHeader("X-Sharer-User-Id") long bookerId,
														  @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
														  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
														  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, bookerId, from, size);
		return bookingClient.getAllBookingByBookerId(bookerId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllBookingByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId,
														 @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
														 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
														 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, ownerId, from, size);
		return bookingClient.getAllBookingByOwnerId(ownerId, state, from, size);
	}
}
