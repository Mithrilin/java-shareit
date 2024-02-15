package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item addItem(Item item);

    Item getItemById(Long id);

    List<Item> getAllItemsByUserId(long userId);

    List<Item> getItemsBySearch(String text);
}
