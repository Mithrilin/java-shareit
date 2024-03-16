package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBooker_IdAndEndIsBefore(long bookerId, LocalDateTime end, PageRequest pageRequest);

    Page<Booking> findByBooker_IdAndStartIsAfter(long bookerId, LocalDateTime start, PageRequest pageRequest);

    @Query(value = "select b from Booking as b " +
            "where b.booker.id = ?1 " +
            "and ?2 between b.start and b.end")
    Page<Booking> findAllByBookerIdWithStateCurrent(long bookerId, LocalDateTime now, PageRequest pageRequest);

    Page<Booking> findByBooker_IdAndStatusEquals(long bookerId, BookingStatus bookingStatus, PageRequest pageRequest);

    Page<Booking> findByBooker_Id(long bookerId, PageRequest pageRequest);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and b.end < ?2")
    Page<Booking> findByOwnerIdWithStatePast(long ownerId, LocalDateTime now, PageRequest pageRequest);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and b.end > ?2")
    Page<Booking> findByOwnerIdWithStateFuture(long ownerId, LocalDateTime now, PageRequest pageRequest);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and ?2 between b.start and b.end")
    Page<Booking> findAllByOwnerIdWithStateCurrent(long ownerId, LocalDateTime now, PageRequest pageRequest);

    @Query(value = "select b from Booking as b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2")
    Page<Booking> findByOwnerIdAndStatus(long bookerId, BookingStatus bookingStatus, PageRequest pageRequest);

    Page<Booking> findByItemOwnerId(long bookerId, PageRequest pageRequest);

    List<Booking> findByItemId(long itemId);

    List<Booking> findByItemIdAndBookerId(long itemId, long bookerId);

    List<Booking> findByItemIdIn(List<Long> itemIds);
}
