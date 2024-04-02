package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemAlreadyBookedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerOrBookerException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.params.PageRequestParams;
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
        User user = isUserPresent(bookerId);
        Item item = isItemPresent(bookingDto.getItemId());
        isBookingValid(bookerId, item);
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Добавлено бронирование с ID = {}", savedBooking.getId());
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(long userId, long bookingId, boolean approved) {
        isUserPresent(userId);
        Booking booking = isBookingPresent(bookingId);
        isUserOwner(userId, booking);
        isBookingApproved(booking);
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
        Booking booking = isBookingPresent(bookingId);
        isUserPresent(userId);
        isUserOwnerOrBooker(userId, booking);
        log.info("Бронирование с ID {} возвращено.", bookingId);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingByBookerId(long bookerId, BookingState bookingState, PageRequestParams pageRequestParams) {
        isUserPresent(bookerId);
        Page<Booking> bookingsPage;
        PageRequest pageRequest = pageRequestParams.getPageRequest();
        switch (bookingState) {
            case PAST:
                bookingsPage = bookingRepository.findByBooker_IdAndEndIsBefore(bookerId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookingsPage = bookingRepository.findByBooker_IdAndStartIsAfter(bookerId, LocalDateTime.now(), pageRequest);
                break;
            case CURRENT:
                bookingsPage = bookingRepository.findAllByBookerIdWithStateCurrent(bookerId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookingsPage = bookingRepository.findByBooker_IdAndStatusEquals(bookerId, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookingsPage = bookingRepository.findByBooker_IdAndStatusEquals(bookerId, BookingStatus.REJECTED, pageRequest);
                break;
            default:
                bookingsPage = bookingRepository.findByBooker_Id(bookerId, pageRequest);
        }
        List<BookingDto> bookingDtos = BookingMapper.toBookingDtos(bookingsPage.getContent());
        log.info("Список бронирований в состоянии {} пользователя с ид {} с номера {} размером {} возвращён.",
                bookingState, bookerId, pageRequestParams.getFrom(), pageRequestParams.getSize());
        return bookingDtos;
    }

    @Override
    public List<BookingDto> getAllBookingByOwnerId(long ownerId, BookingState bookingState, PageRequestParams pageRequestParams) {
        isUserPresent(ownerId);
        Page<Booking> bookingsPage;
        PageRequest pageRequest = pageRequestParams.getPageRequest();
        switch (bookingState) {
            case PAST:
                bookingsPage = bookingRepository.findByOwnerIdWithStatePast(ownerId, LocalDateTime.now(), pageRequest);
                break;
            case FUTURE:
                bookingsPage = bookingRepository.findByOwnerIdWithStateFuture(ownerId, LocalDateTime.now(), pageRequest);
                break;
            case CURRENT:
                bookingsPage = bookingRepository.findByByOwnerIdWithStateCurrent(ownerId, LocalDateTime.now(), pageRequest);
                break;
            case WAITING:
                bookingsPage = bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookingsPage = bookingRepository.findByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, pageRequest);
                break;
            default:
                bookingsPage = bookingRepository.findByItemOwnerId(ownerId, pageRequest);
        }
        List<BookingDto> bookingDtos = BookingMapper.toBookingDtos(bookingsPage.getContent());
        log.info("Список бронирований в состоянии {} владельца вещей с ид {} с номера {} размером {} возвращён.",
                bookingState, ownerId, pageRequestParams.getFrom(), pageRequestParams.getSize());
        return bookingDtos;
    }

    private void isBookingValid(long bookerId,
                                Item item) {
        if (item.getOwner().getId() == bookerId) {
            log.error("Пользователь с ИД {} является владельцем вещи с ИД {}.", bookerId, item.getId());
            throw new NotOwnerOrBookerException(String.format("Пользователь с ИД %d является владельцем вещи с ИД %d.",
                    bookerId, item.getId()));
        }
        if (!item.getAvailable()) {
            log.error("Вещь с ИД {} уже забронирована.", item.getId());
            throw new ItemAlreadyBookedException(String.format("Вещь с ИД %d уже забронирована.", item.getId()));
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

    private User isUserPresent(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        return optionalUser.get();
    }

    private Item isItemPresent(long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            log.error("Вещь с ИД {} отсутствует в БД.", itemId);
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", itemId));
        }
        return optionalItem.get();
    }

    private Booking isBookingPresent(long bookingId) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            log.error("Бронирование с ИД {} отсутствует в БД.", bookingId);
            throw new NotFoundException(String.format("Бронирование с ИД %d отсутствует в БД.", bookingId));
        }
        return optionalBooking.get();
    }

    private void isBookingApproved(Booking booking) {
        if (booking.getStatus() == BookingStatus.APPROVED) {
            log.error("Нельзя менять статус после одобрения.");
            throw new NotValidException("Нельзя менять статус после одобрения.");
        }
    }
}
