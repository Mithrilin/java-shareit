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

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        if (itemDto.getName() == null
                || itemDto.getName().isBlank()
                || itemDto.getDescription() == null
                || itemDto.getDescription().isBlank()
                || itemDto.getAvailable() == null) {
            throw new RuntimeException("Вещь не прошла проверку.");
        }
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
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
        if (item == null) {
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", itemDto.getId()));
        }
        if (item.getOwner().getId() != userId) {
            throw new NotFoundException(String.format("Пользователь с ИД %d не является владельцем вещи с ИД %d.",
                    userId, item.getId()));
        }
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
}
