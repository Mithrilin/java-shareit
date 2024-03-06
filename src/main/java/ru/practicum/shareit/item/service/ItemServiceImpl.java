package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        isItemDtoValid(itemDto);
        User user = userRepository.getReferenceById(userId);
        isUserPresent(user, userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        item = itemRepository.addItem(item);
        itemDto.setId(item.getId());
        log.info("Добавлена новая вещь с ID = {}", item.getId());
        return itemDto;
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Item item = itemRepository.getItemById(itemDto.getId());
        isItemPresent(item, itemDto.getId());
        isUserOwner(item, userId);
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        log.info("Вещь с ID {} обновлена.", itemDto.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(long id) {
        Item item = itemRepository.getItemById(id);
        isItemPresent(item, id);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        log.info("Вещь с ID {} возвращена.", id);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        User user = userRepository.getReferenceById(userId);
        isUserPresent(user, userId);
        List<Item> items = itemRepository.getAllItemsByUserId(userId);
        List<ItemDto> itemsDto = items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        log.info("Текущее количество вещей пользователя с ид {} составляет: {} шт. Список возвращён.",
                userId, items.size());
        return itemsDto;
    }

    @Override
    public List<ItemDto> getItemsBySearch(String text) {
        List<Item> items = itemRepository.getItemsBySearch(text.toLowerCase());
        List<ItemDto> itemsDto = items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        log.info("Текущее количество свободных вещей по запросу \"{}\" составляет: {} шт. Список возвращён.",
                text, items.size());
        return itemsDto;
    }

    private void isUserPresent(User user, Long id) {
        if (user == null) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", id));
        }
    }

    private void isItemPresent(Item item, Long id) {
        if (item == null) {
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", id));
        }
    }

    private void isUserOwner(Item item, long userId) {
        if (item.getOwner().getId() != userId) {
            throw new NotFoundException(String.format("Пользователь с ИД %d не является владельцем вещи с ИД %d.",
                    userId, item.getId()));
        }
    }

    private void isItemDtoValid(ItemDto itemDto) {
        if (itemDto.getName() == null
                || itemDto.getName().isBlank()
                || itemDto.getDescription() == null
                || itemDto.getDescription().isBlank()
                || itemDto.getAvailable() == null) {
            throw new RuntimeException("Вещь не прошла проверку.");
        }
    }
}
