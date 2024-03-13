package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_IdAndEndIsBeforeOrderByIdDesc(long bookerId, LocalDateTime end);

    List<Booking> findByBooker_IdAndStartIsAfterOrderByIdDesc(long bookerId, LocalDateTime start);

    @Query(value = "select b from Booking as b " +
            "where b.booker.id = ?1 " +
            "and ?2 between b.start and b.end")
    List<Booking> findAllByBookerIdWithStateCurrent(long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBooker_IdAndStatusEqualsOrderByIdDesc(long bookerId, BookingStatus bookingStatus);

    List<Booking> findByBooker_IdOrderByIdDesc(long bookerId, Sort sort);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and b.end < ?2")
    List<Booking> findByOwnerIdWithStatePast(long ownerId, LocalDateTime now, Sort sort);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and b.end > ?2")
    List<Booking> findByOwnerIdWithStateFuture(long ownerId, LocalDateTime now, Sort sort);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 between b.start and b.end")
    List<Booking> findAllByOwnerIdWithStateCurrent(long ownerId, LocalDateTime now, Sort sort);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2")
    List<Booking> findByOwnerIdAndStatus(long bookerId, BookingStatus bookingStatus, Sort sort);

    List<Booking> findByItemOwnerIdOrderByIdDesc(long bookerId);

    List<Booking> findByItemId(long itemId);

    List<Booking> findByItemIdAndBookerId(long itemId, long bookerId);

    List<Booking> findByItemIdIn(List<Long> itemIds);
}
