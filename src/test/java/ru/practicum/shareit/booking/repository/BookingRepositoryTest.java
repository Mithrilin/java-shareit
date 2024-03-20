package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {
    private List<User> users = null;
    private List<Item> items = null;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        users = usersBuilder();
        items = itemBuilder();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии PAST для пользователя с ид 1, когда БД пустая")
    void findByBooker_IdAndEndIsBefore_whenEmptyDatabase_thenReturnedEmptyList() {
        long bookerId = 1;
        int from = 0;
        int size = 1;
        int page = from/size;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndIsBefore(bookerId, LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение списка бронирований в состоянии PAST для пользователя с ид 1, когда 1 бронирование")
    void findByBooker_IdAndEndIsBefore_when1CorrectBooking_thenReturned1Booking() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndIsBefore(user.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        booking.setId(bookings.get(0).getId());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии PAST для пользователя с ид 1, когда нет бронирований " +
            "пользователя с ид 1")
    void findByBooker_IdAndEndIsBefore_whenBookingWithoutCorrectBookerId_thenReturnedEmptyList() {
        User user1 = userRepository.save(users.get(0));
        User user2 = userRepository.save(users.get(1));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user1, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndIsBefore(user2.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии PAST для пользователя с ид 1, когда нет бронирований " +
            "с состоянием PAST")
    void findByBooker_IdAndEndIsBefore_whenBookingWithoutCorrectEnd_thenReturnedEmptyList() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndEndIsBefore(user.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии WAITING для пользователя с ид 1, когда БД пустая")
    void findByBooker_IdAndStatusEquals_whenEmptyDatabase_thenReturnedEmptyList() {
        long bookerId = 1;
        int from = 0;
        int size = 1;
        int page = from/size;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Booking> bookings = bookingRepository.findByBooker_IdAndStatusEquals(bookerId, BookingStatus.WAITING,
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение списка бронирований в состоянии WAITING для пользователя с ид 1, когда 1 бронирование")
    void findByBooker_IdAndStatusEquals_when1CorrectBooking_thenReturned1Booking() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndStatusEquals(user.getId(), BookingStatus.WAITING,
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        booking.setId(bookings.get(0).getId());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии WAITING для пользователя с ид 1, когда нет бронирований " +
            "пользователя с ид 1")
    void findByBooker_IdAndStatusEquals_whenBookingWithoutCorrectBookerId_thenReturnedEmptyList() {
        User user1 = userRepository.save(users.get(0));
        User user2 = userRepository.save(users.get(1));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user1, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndStatusEquals(user2.getId(), BookingStatus.WAITING,
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии REJECTED для пользователя с ид 1, когда нет бронирований " +
            "с состоянием REJECTED")
    void findByBooker_IdAndStatusEquals_whenBookingWithoutCorrectEnd_thenReturnedEmptyList() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndStatusEquals(user.getId(), BookingStatus.REJECTED,
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии CURRENT для пользователя с ид 1, когда БД пустая")
    void findAllByBookerIdWithStateCurrent_whenEmptyDatabase_thenReturnedEmptyList() {
        long bookerId = 1;
        int from = 0;
        int size = 1;
        int page = from/size;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Booking> bookings = bookingRepository.findAllByBookerIdWithStateCurrent(bookerId, LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение списка бронирований в состоянии CURRENT для пользователя с ид 1, когда 1 бронирование")
    void findAllByBookerIdWithStateCurrent_when1CorrectBooking_thenReturned1Booking() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findAllByBookerIdWithStateCurrent(user.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        booking.setId(bookings.get(0).getId());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии CURRENT для пользователя с ид 1, когда нет бронирований " +
            "пользователя с ид 1")
    void findAllByBookerIdWithStateCurrent_whenBookingWithoutCorrectBookerId_thenReturnedEmptyList() {
        User user1 = userRepository.save(users.get(0));
        User user2 = userRepository.save(users.get(1));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user1, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findAllByBookerIdWithStateCurrent(user2.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии CURRENT для пользователя с ид 1, когда нет бронирований " +
            "с состоянием CURRENT")
    void findAllByBookerIdWithStateCurrent_whenBookingWithoutCorrectEnd_thenReturnedEmptyList() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findAllByBookerIdWithStateCurrent(user.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии FUTURE для пользователя с ид 1, когда БД пустая")
    void findByBooker_IdAndStartIsAfter_whenEmptyDatabase_thenReturnedEmptyList() {
        long bookerId = 1;
        int from = 0;
        int size = 1;
        int page = from/size;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Booking> bookings = bookingRepository.findByBooker_IdAndStartIsAfter(bookerId, LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение списка бронирований в состоянии FUTURE для пользователя с ид 1, когда 1 бронирование")
    void findByBooker_IdAndStartIsAfter_when1CorrectBooking_thenReturned1Booking() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndStartIsAfter(user.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertEquals(1, bookings.size());
        booking.setId(bookings.get(0).getId());
        assertEquals(booking, bookings.get(0));
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии FUTURE для пользователя с ид 1, когда нет бронирований " +
            "пользователя с ид 1")
    void findByBooker_IdAndStartIsAfter_whenBookingWithoutCorrectBookerId_thenReturnedEmptyList() {
        User user1 = userRepository.save(users.get(0));
        User user2 = userRepository.save(users.get(1));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user1, item);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndStartIsAfter(user2.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("Получение пустого списка бронирований в состоянии FUTURE для пользователя с ид 1, когда нет бронирований " +
            "с состоянием FUTURE")
    void findByBooker_IdAndStartIsAfter_whenBookingWithoutCorrectEnd_thenReturnedEmptyList() {
        User user = userRepository.save(users.get(0));
        Item item = itemRepository.save(items.get(0));
        Booking booking = bookingBuilder(user, item);
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        booking.setStart(start);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        booking.setEnd(end);
        bookingRepository.save(booking);
        int from = 0;
        int size = 1;
        int page = from/size;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "start"));

        List<Booking> bookings = bookingRepository.findByBooker_IdAndStartIsAfter(user.getId(), LocalDateTime.now(),
                pageRequest).getContent();

        assertTrue(bookings.isEmpty());
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
            item.setOwner(users.get(i-1));
            items.add(item);
        }
        return items;
    }
}