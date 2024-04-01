package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.params.PageRequestParams;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    private List<User> users = null;
    private List<Item> items = null;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @InjectMocks
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        users = usersBuilder();
        items = itemBuilder();
    }

    @Test
    @DisplayName("Успешное добавление бронирования, когда бронирование валидное")
    void addBooking_whenBookingIsValid_thenReturnedBookingDto() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        BookingDto bookingDto = new BookingDto(null, itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, null);
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(user2);
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto returnedBookingDto = bookingService.addBooking(user2Id, bookingDto);

        assertEquals(UserMapper.toUserDto(user2), returnedBookingDto.getBooker());
        assertEquals(ItemMapper.toItemDto(item), returnedBookingDto.getItem());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при добавлении бронирования, когда пользователь отсутствует в БД")
    void addBooking_whenUserNotPresent_thenThrowNotFoundException() {
        long user2Id = 1L;
        when(userRepository.findById(user2Id)).thenReturn(Optional.empty());
        long itemId = 1L;
        BookingDto bookingDto = new BookingDto(null, itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.addBooking(user2Id, bookingDto));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(itemRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при добавлении бронирования, когда вещь отсутствует в БД")
    void addBooking_whenItemNotPresent_thenThrowNotFoundException() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        long itemId = 1L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        BookingDto bookingDto = new BookingDto(null, itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.addBooking(user2Id, bookingDto));

        assertEquals("Вещь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка NotOwnerOrBookerException при добавлении бронирования, когда пользователь является владельцем")
    void addBooking_whenBookingIsValid_thenThrowNotOwnerOrBookerException() {
        long user2Id = 2L;
        User user2 = users.get(0);
        user2.setId(user2Id);
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        BookingDto bookingDto = new BookingDto(null, itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, null);

        NotOwnerOrBookerException exception = assertThrows(
                NotOwnerOrBookerException.class,
                () -> bookingService.addBooking(user2Id, bookingDto));

        assertEquals("Пользователь с ИД 2 является владельцем вещи с ИД 1.", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка ItemAlreadyBookedException при добавлении бронирования, когда вещь уже забронирована")
    void addBooking_whenAvailableIsFalse_thenThrowItemAlreadyBookedException() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        item.setAvailable(false);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        BookingDto bookingDto = new BookingDto(null, itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, null);

        ItemAlreadyBookedException exception = assertThrows(
                ItemAlreadyBookedException.class,
                () -> bookingService.addBooking(user2Id, bookingDto));

        assertEquals("Вещь с ИД 1 уже забронирована.", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное подтверждение бронирования, когда пользователь владелец.")
    void approveBooking_whenStatusIsApproved_thenReturnedBookingDto() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        long bookingId = 1L;
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        Booking booking = new Booking(bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, user2, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto returnedBookingDto = bookingService.approveBooking(user1Id, bookingId, true);

        assertEquals(UserMapper.toUserDto(user2), returnedBookingDto.getBooker());
        assertEquals(ItemMapper.toItemDto(item), returnedBookingDto.getItem());
        assertEquals(BookingStatus.APPROVED, returnedBookingDto.getStatus());
    }

    @Test
    @DisplayName("Успешный отказ в бронирования, когда пользователь владелец.")
    void approveBooking_whenStatusIsRejected_thenReturnedBookingDto() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        long bookingId = 1L;
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        Booking booking = new Booking(bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, user2, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingDto returnedBookingDto = bookingService.approveBooking(user1Id, bookingId, false);

        assertEquals(UserMapper.toUserDto(user2), returnedBookingDto.getBooker());
        assertEquals(ItemMapper.toItemDto(item), returnedBookingDto.getItem());
        assertEquals(BookingStatus.REJECTED, returnedBookingDto.getStatus());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при подтверждении бронирования, когда такого бронирования нет в БД.")
    void approveBooking_whenBookingNotPresent_thenNotFoundException() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        long bookingId = 1L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.approveBooking(user1Id, bookingId, true));

        assertEquals("Бронирование с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка NotOwnerOrBookerException при добавлении бронирования, когда пользователь не владелец.")
    void approveBooking_whenUserNotOwner_thenThrowNotOwnerOrBookerException() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        long bookingId = 1L;
        Booking booking = new Booking(bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, user2, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        NotOwnerOrBookerException exception = assertThrows(
                NotOwnerOrBookerException.class,
                () -> bookingService.approveBooking(user2Id, bookingId, true));

        assertEquals("Пользователь с ИД 2 не является владельцем вещи с ИД 1.", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка NotValidException при добавлении бронирования, когда бронирование уже одобрено.")
    void approveBooking_whenStatusAlreadyApproved_thenThrowNotValidException() {
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        long bookingId = 1L;
        Booking booking = new Booking(bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, user2, BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        NotValidException exception = assertThrows(
                NotValidException.class,
                () -> bookingService.approveBooking(user1Id, bookingId, true));

        assertEquals("Нельзя менять статус после одобрения.", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение бронирования по ид, когда пользователь владелец.")
    void getBookingById_whenUserIsOwner_thenReturnedBookingDto() {
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        long bookingId = 1L;
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        Booking booking = new Booking(bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, user2, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        long user1Id = 1L;
        User user1 = users.get(0);
        user1.setId(user1Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));

        BookingDto returnedBookingDto = bookingService.getBookingById(user1Id, bookingId);

        assertEquals(UserMapper.toUserDto(user2), returnedBookingDto.getBooker());
        assertEquals(ItemMapper.toItemDto(item), returnedBookingDto.getItem());
        assertEquals(BookingStatus.WAITING, returnedBookingDto.getStatus());
    }

    @Test
    @DisplayName("Ошибка NotOwnerOrBookerException при получении бронирования по ид, когда пользователь не владелец " +
            "или создатель бронирования.")
    void getBookingById_whenUserNotOwnerOrBooker_thenThrowNotOwnerOrBookerException() {
        long itemId = 1L;
        Item item = items.get(0);
        item.setId(itemId);
        long bookingId = 1L;
        long userId = 1L;
        User user = users.get(0);
        user.setId(userId);
        long user2Id = 2L;
        User user2 = users.get(1);
        user2.setId(user2Id);
        Booking booking = new Booking(bookingId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, user2, BookingStatus.WAITING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        long user1Id = 3L;
        User user1 = users.get(2);
        user1.setId(user1Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));

        NotOwnerOrBookerException exception = assertThrows(
                NotOwnerOrBookerException.class,
                () -> bookingService.getBookingById(user1Id, bookingId));

        assertEquals("Пользователь с ИД 3 не является владельцем или заказчиком вещи.", exception.getMessage());
    }

    @Test
    @DisplayName("Получение списка бронирований по ид заказчика, когда состояние PAST.")
    void getAllBookingByBookerId_whenStateIsPast_thenReturnedBookingDtoList() {
        long userId = 1L;
        User booker = users.get(2);
        booker.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByBooker_IdAndEndIsBefore(anyLong(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByBookerId(userId, BookingState.PAST,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид заказчика, когда состояние FUTURE.")
    void getAllBookingByBookerId_whenStateIsFuture_thenReturnedBookingDtoList() {
        long userId = 1L;
        User booker = users.get(2);
        booker.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByBooker_IdAndStartIsAfter(anyLong(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByBookerId(userId, BookingState.FUTURE,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид заказчика, когда состояние CURRENT.")
    void getAllBookingByBookerId_whenStateIsCurrent_thenReturnedBookingDtoList() {
        long userId = 1L;
        User booker = users.get(2);
        booker.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findAllByBookerIdWithStateCurrent(anyLong(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByBookerId(userId, BookingState.CURRENT,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид заказчика, когда состояние WAITING.")
    void getAllBookingByBookerId_whenStateIsWaiting_thenReturnedBookingDtoList() {
        long userId = 1L;
        User booker = users.get(2);
        booker.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByBooker_IdAndStatusEquals(anyLong(), any(BookingStatus.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByBookerId(userId, BookingState.WAITING,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид заказчика, когда состояние REJECTED.")
    void getAllBookingByBookerId_whenStateIsRejected_thenReturnedBookingDtoList() {
        long userId = 1L;
        User booker = users.get(2);
        booker.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.REJECTED);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.REJECTED);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByBooker_IdAndStatusEquals(anyLong(), any(BookingStatus.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByBookerId(userId, BookingState.REJECTED,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид заказчика, когда состояние ALL.")
    void getAllBookingByBookerId_whenStateIsAll_thenReturnedBookingDtoList() {
        long userId = 1L;
        User booker = users.get(2);
        booker.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByBooker_Id(anyLong(), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByBookerId(userId, BookingState.ALL,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид владельца вещей, когда состояние PAST.")
    void getAllBookingByOwnerId_whenStateIsPast_thenReturnedBookingDtoList() {
        long ownerId = 1L;
        User owner = users.get(0);
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        item2.setOwner(owner);
        User booker = users.get(2);
        booker.setId(1L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByOwnerIdWithStatePast(anyLong(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwnerId(ownerId, BookingState.PAST,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид владельца вещей, когда состояние FUTURE.")
    void getAllBookingByOwnerId_whenStateIsFuture_thenReturnedBookingDtoList() {
        long ownerId = 1L;
        User owner = users.get(0);
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        item2.setOwner(owner);
        User booker = users.get(2);
        booker.setId(1L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByOwnerIdWithStateFuture(anyLong(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwnerId(ownerId, BookingState.FUTURE,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид владельца вещей, когда состояние CURRENT.")
    void getAllBookingByOwnerId_whenStateIsCurrent_thenReturnedBookingDtoList() {
        long ownerId = 1L;
        User owner = users.get(0);
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        item2.setOwner(owner);
        User booker = users.get(2);
        booker.setId(1L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByByOwnerIdWithStateCurrent(anyLong(), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwnerId(ownerId, BookingState.CURRENT,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид владельца вещей, когда состояние WAITING.")
    void getAllBookingByOwnerId_whenStateIsWaiting_thenReturnedBookingDtoList() {
        long ownerId = 1L;
        User owner = users.get(0);
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        item2.setOwner(owner);
        User booker = users.get(2);
        booker.setId(1L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByOwnerIdAndStatus(anyLong(), any(BookingStatus.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwnerId(ownerId, BookingState.WAITING,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид владельца вещей, когда состояние REJECTED.")
    void getAllBookingByOwnerId_whenStateIsRejected_thenReturnedBookingDtoList() {
        long ownerId = 1L;
        User owner = users.get(0);
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        item2.setOwner(owner);
        User booker = users.get(2);
        booker.setId(1L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.REJECTED);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.REJECTED);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByOwnerIdAndStatus(anyLong(), any(BookingStatus.class), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwnerId(ownerId, BookingState.REJECTED,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    @Test
    @DisplayName("Получение списка бронирований по ид владельца вещей, когда состояние ALL.")
    void getAllBookingByOwnerId_whenStateIsAll_thenReturnedBookingDtoList() {
        long ownerId = 1L;
        User owner = users.get(0);
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        Item item1 = items.get(0);
        item1.setId(1L);
        Item item2 = items.get(1);
        item2.setId(2L);
        item2.setOwner(owner);
        User booker = users.get(2);
        booker.setId(1L);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(2),
                item1, booker, BookingStatus.WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(3),
                item2, booker, BookingStatus.WAITING);
        Page<Booking> bookingsPage = new PageImpl<>(List.of(booking1, booking2));
        when(bookingRepository.findByItemOwnerId(anyLong(), any(PageRequest.class)))
                .thenReturn(bookingsPage);

        List<BookingDto> bookingDtos = bookingService.getAllBookingByOwnerId(ownerId, BookingState.ALL,
                new PageRequestParams(0, 20, Sort.Direction.DESC, "start"));

        assertEquals(2, bookingDtos.size());
        assertEquals(BookingMapper.toBookingDto(booking1), bookingDtos.get(0));
        assertEquals(BookingMapper.toBookingDto(booking2), bookingDtos.get(1));
    }

    private Booking bookingBuilder(User user, Item item) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    private List<User> usersBuilder() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            users.add(
                    new User(
                            "name" + i,
                            "name" + i + "@mail.ru"
                    )
            );
        }
        return users;
    }

    private List<Item> itemBuilder() {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            Item item = new Item(
                    "name" + i,
                    "description" + i,
                    true
            );
            item.setOwner(users.get(i - 1));
            items.add(item);
        }
        return items;
    }
}