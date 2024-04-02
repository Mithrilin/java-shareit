package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.params.PageRequestParams;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto addItemRequest(long userId, ItemRequestDto itemRequestDto) {
        User user = isUserPresent(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        log.info("Добавлен новый запрос с ID = {}", savedItemRequest.getId());
        return ItemRequestMapper.toItemRequestDto(savedItemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllItemRequestsByUserId(long userId) {
        isUserPresent(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequestorIdOrderByIdDesc(userId);
        List<ItemRequestDto> itemRequestDtos = addItemDtos(itemRequests);
        log.info("Текущее количество запросов пользователя с ид {} составляет: {} шт. Список возвращён.",
                userId, itemRequestDtos.size());
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(long userId, PageRequestParams pageRequestParams) {
        isUserPresent(userId);
        PageRequest pageRequest = pageRequestParams.getPageRequest();
        Page<ItemRequest> itemRequestPage = itemRequestRepository.findByRequestorIdNot(userId, pageRequest);
        List<ItemRequest> itemRequests = itemRequestPage.getContent();
        List<ItemRequestDto> itemRequestDtos = addItemDtos(itemRequests);
        log.info("Список запросов с номера {} размером {} возвращён.", pageRequestParams.getFrom(), pageRequestParams.getSize());
        return itemRequestDtos;
    }

    @Override
    public ItemRequestDto getItemRequestById(long userId, long itemRequestId) {
        isUserPresent(userId);
        ItemRequest itemRequest = isItemRequestPresent(itemRequestId);
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        List<ItemDto> itemDtos = ItemMapper.toItemDtos(items);
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(itemDtos);
        log.info("Запрос с ID {} возвращен.", itemRequestId);
        return itemRequestDto;
    }

    private ItemRequest isItemRequestPresent(long itemRequestId) {
        Optional<ItemRequest> optionalItemRequest = itemRequestRepository.findById(itemRequestId);
        if (optionalItemRequest.isEmpty()) {
            log.error("Запрос с ИД {} отсутствует в БД.", itemRequestId);
            throw new NotFoundException(String.format("Запрос с ИД %d отсутствует в БД.", itemRequestId));
        }
        return optionalItemRequest.get();
    }

    private User isUserPresent(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        return optionalUser.get();
    }

    private List<ItemRequestDto> addItemDtos(List<ItemRequest> itemRequests) {
        List<ItemRequestDto> itemRequestDtos = ItemRequestMapper.toItemRequestDtos(itemRequests);
        List<Long> itemRequestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findByRequestIdIn(itemRequestIds);
        if (!items.isEmpty()) {
            List<ItemDto> itemDtos = ItemMapper.toItemDtos(items);
            Map<Long, List<ItemDto>> itemRequestIdToItems = itemDtos.stream()
                    .collect(Collectors.groupingBy(ItemDto::getRequestId, Collectors.toList()));
            itemRequestDtos.forEach(itemRequestDto -> itemRequestDto.setItems(itemRequestIdToItems.get(itemRequestDto.getId())));
        }
        return itemRequestDtos;
    }
}
