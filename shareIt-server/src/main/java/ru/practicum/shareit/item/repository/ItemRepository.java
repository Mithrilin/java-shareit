package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByOwnerId(long userId, PageRequest pageRequest);

    @Query(value = "select it from Item as it " +
            "where it.available = true " +
            "and (UPPER(it.name) like UPPER(concat('%', ?1, '%')) " +
            "or UPPER(it.description) like UPPER(concat('%', ?1, '%')))")
    Page<Item> findAllBySearch(String text, PageRequest pageRequest);

    List<Item> findByRequestIdIn(List<Long> itemRequestIds);

    List<Item> findByRequestId(Long id);
}
