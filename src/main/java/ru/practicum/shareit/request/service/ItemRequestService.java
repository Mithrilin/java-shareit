package ru.practicum.shareit.request.service;

import ru.practicum.shareit.params.PageRequestParams;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto addItemRequest(long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAllItemRequestsByUserId(long userId);

    List<ItemRequestDto> getAllItemRequests(long userId, PageRequestParams pageRequestParams);

    ItemRequestDto getItemRequestById(long userId, long itemRequestId);
}
