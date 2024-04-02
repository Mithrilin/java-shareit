package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemGetResponseDto;
import ru.practicum.shareit.params.PageRequestParams;

import java.util.List;

public interface ItemService {

    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, ItemDto itemDto);

    ItemGetResponseDto getItemById(long userId, long id);

    List<ItemGetResponseDto> getAllItemsByUserId(long userId, PageRequestParams pageRequestParams);

    List<ItemDto> getItemsBySearch(String text, PageRequestParams pageRequestParams);

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);
}
