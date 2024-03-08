package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerIdOrderByIdAsc(long userId);

    @Query(value = "select it from Item as it " +
            "where it.available = true " +
            "and (UPPER(it.name) like UPPER(concat('%', ?1, '%')) " +
            "or UPPER(it.description) like UPPER(concat('%', ?1, '%')))")
    List<Item> findAllBySearch(String text);
}
