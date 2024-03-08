package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerOrBookerException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public BookingDto addBooking(long bookerId, BookingDto bookingDto) {
        Optional<User> optionalUser = userRepository.findById(bookerId);
        Optional<Item> optionalItem = itemRepository.findById(bookingDto.getItemId());
        isBookingValid(optionalUser, optionalItem, bookerId, bookingDto);
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(optionalItem.orElseThrow());
        booking.setBooker(optionalUser.orElseThrow());
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Добавлено бронирование с ID = {}", savedBooking.getId());
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(long userId, long bookingId, boolean approved) {
        Optional<User> optionalUser = userRepository.findById(userId);
        isUserPresent(optionalUser, userId);
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        isBookingPresent(optionalBooking, bookingId);
        Booking booking = optionalBooking.orElseThrow();
        isUserOwner(userId, booking);
        if (booking.getStatus() == BookingStatus.APPROVED) {
            log.error("Нельзя менять статус после одобрения.");
            throw new NotValidException("Нельзя менять статус после одобрения.");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        bookingRepository.save(booking);
        log.info("Бронирование с ID = {} одобрено владельцем вещи.", bookingId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(long userId, long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        isBookingPresent(optionalBooking, bookingId);
        Booking booking = optionalBooking.orElseThrow();
        Optional<User> optionalUser = userRepository.findById(userId);
        isUserPresent(optionalUser, userId);
        isUserOwnerOrBooker(userId, booking);
        log.info("Бронирование с ID {} возвращено.", bookingId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingByBookerId(long bookerId, String state) {
        Optional<User> optionalUser = userRepository.findById(bookerId);
        isUserPresent(optionalUser, bookerId);
        List<Booking> bookings;
        BookingState bookingState = checkStateValue(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        switch (bookingState) {
            case PAST:
                bookings = bookingRepository.findByBooker_IdAndEndIsBeforeOrderByIdDesc(bookerId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findByBooker_IdAndStartIsAfterOrderByIdDesc(bookerId, LocalDateTime.now());
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdWithStateCurrent(bookerId, LocalDateTime.now(),
                        Sort.by(Sort.Direction.ASC, "id"));
                break;
            case WAITING:
                bookings = bookingRepository.findByBooker_IdAndStatusEqualsOrderByIdDesc(bookerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBooker_IdAndStatusEqualsOrderByIdDesc(bookerId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findByBooker_IdOrderByIdDesc(bookerId, sort);
        }
        List<BookingDto> bookingDtos = BookingMapper.toBookingDtos(bookings);
        log.info("Текущее количество бронирований в состоянии {} пользователя с ид {} составляет: {} шт. Список возвращён.",
                state, bookerId, bookings.size());
        return bookingDtos;
    }

    @Override
    public List<BookingDto> getAllBookingByOwnerId(long ownerId, String state) {
        Optional<User> optionalUser = userRepository.findById(ownerId);
        isUserPresent(optionalUser, ownerId);
        List<Booking> bookings;
        BookingState bookingState = checkStateValue(state);
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        switch (bookingState) {
            case PAST:
                bookings = bookingRepository.findByOwnerIdWithStatePast(ownerId, LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByOwnerIdWithStateFuture(ownerId, LocalDateTime.now(), sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdWithStateCurrent(ownerId, LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, sort);
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByIdDesc(ownerId);
        }
        List<BookingDto> bookingDtos = BookingMapper.toBookingDtos(bookings);
        log.info("Текущее количество бронирований в состоянии {} владельца вещей с ид {} составляет: {} шт. Список возвращён.",
                state, ownerId, bookings.size());
        return bookingDtos;
    }

    private BookingState checkStateValue(String state) {
        BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: {}", state);
            throw new NotValidException("Unknown state: " + state);
        }
        return bookingState;
    }

    private void isBookingValid(Optional<User> optionalUser,
                                Optional<Item> optionalItem,
                                long bookerId,
                                BookingDto bookingDto) {
        isUserPresent(optionalUser, bookerId);
        if (optionalItem.isEmpty()) {
            log.error("Вещь с ИД {} отсутствует в БД.", bookingDto.getItemId());
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", bookingDto.getItemId()));
        }
        Item item = optionalItem.get();
        if (item.getOwner().getId() == bookerId) {
            log.error("Пользователь с ИД {} является владельцем вещи с ИД {}.", bookerId, item.getId());
            throw new NotOwnerOrBookerException(String.format("Пользователь с ИД %d является владельцем вещи с ИД %d.",
                    bookerId, item.getId()));
        }
        if (!item.getAvailable()) {
            log.error("Вещь с ИД {} уже забронирована.", item.getId());
            throw new NotValidException(String.format("Вещь с ИД %d уже забронирована.", item.getId()));
        }
        if (bookingDto.getStart() == null
                || bookingDto.getEnd() == null
                || bookingDto.getStart().isBefore(LocalDateTime.now())
                || !bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            log.error("Даты бронирования заданы неверно.");
            throw new NotValidException("Даты бронирования заданы неверно.");
        }
    }

    private void isUserOwner(long userId, Booking booking) {
        long ownerId = booking.getItem().getOwner().getId();
        if (userId != ownerId) {
            log.error("Пользователь с ИД {} не является владельцем вещи с ИД {}.", userId, booking.getItem().getId());
            throw new NotOwnerOrBookerException(String.format("Пользователь с ИД %d не является владельцем вещи с ИД %d.",
                    userId, booking.getItem().getId()));
        }
    }

    private void isUserOwnerOrBooker(long userId, Booking booking) {
        if (!(userId == booking.getBooker().getId()
                || userId == booking.getItem().getOwner().getId())) {
            log.error("Пользователь с ИД {} не является владельцем или заказчиком вещи.", userId);
            throw new NotOwnerOrBookerException(String.format("Пользователь с ИД %d не является владельцем или заказчиком вещи.",
                    userId));
        }
    }

    private void isUserPresent(Optional<User> optionalUser, long userId) {
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
    }

    private void isBookingPresent(Optional<Booking> optionalBooking, long bookingId) {
        if (optionalBooking.isEmpty()) {
            log.error("Бронирование с ИД {} отсутствует в БД.", bookingId);
            throw new NotFoundException(String.format("Бронирование с ИД %d отсутствует в БД.", bookingId));
        }
    }
}
