package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        isItemDtoValid(itemDto);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(optionalUser.get());
        Item savedItem = itemRepository.save(item);
        log.info("Добавлена новая вещь с ID = {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        Optional<Item> optionalItem = itemRepository.findById(itemDto.getId());
        if (optionalItem.isEmpty()) {
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", itemDto.getId()));
        }
        Item oldItem = optionalItem.get();
        isUserOwner(oldItem, userId);
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }
        Item updatedItem = itemRepository.save(oldItem);
        log.info("Вещь с ID {} обновлена.", updatedItem.getId());
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(long id) {
        Optional<Item> optionalItem = itemRepository.findById(id);
        if (optionalItem.isEmpty()) {
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", id));
        }
        ItemDto itemDto = ItemMapper.toItemDto(optionalItem.get());
        log.info("Вещь с ID {} возвращена.", id);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDto> itemDtos = ItemMapper.toItemDtos(items);
        log.info("Текущее количество вещей пользователя с ид {} составляет: {} шт. Список возвращён.",
                userId, items.size());
        return itemDtos;
    }

    @Override
    public List<ItemDto> getItemsBySearch(String text) {
        String param = "%" + text + "%";
        List<Item> items = itemRepository.findAllBySearch(param);
        List<ItemDto> itemsDto = ItemMapper.toItemDtos(items);
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
            throw new ValidationException("Вещь не прошла проверку.");
        }
    }
}
