package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto);

    ItemDto getItemById(long id);

    List<ItemDto> getAllItemsByUserId(long userId);

    List<ItemDto> getItemsBySearch(String text);
}
