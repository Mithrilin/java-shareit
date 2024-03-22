package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.mapper.CommentMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerOrBookerException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemGetResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto addItem(long userId, ItemDto itemDto) {
        isItemDtoValid(itemDto);
        User user = isUserPresent(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = isItemRequestPresent(itemDto.getRequestId());
            item.setRequest(itemRequest);
        }
        Item savedItem = itemRepository.save(item);
        log.info("Добавлена новая вещь с ID = {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(long userId, ItemDto itemDto) {
        isUserPresent(userId);
        Item oldItem = isItemPresent(itemDto.getId());
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
    public ItemGetResponseDto getItemById(long userId, long itemId) {
        Item item = isItemPresent(itemId);
        ItemGetResponseDto itemGetResponseDto = ItemMapper.toItemGetResponseDto(item);
        List<Booking> bookings = bookingRepository.findByItemId(itemGetResponseDto.getId());
        if (userId == item.getOwner().getId()) {
            addBookingResponseDto(itemGetResponseDto, bookings);
        }
        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemGetResponseDto.setComments(CommentMapper.toCommentDtos(comments));
        log.info("Вещь с ID {} возвращена.", itemId);
        return itemGetResponseDto;
    }

    @Override
    public List<ItemGetResponseDto> getAllItemsByUserId(long userId, int from, int size) {
        isUserPresent(userId);
        List<ItemGetResponseDto> itemGetResponseDtoList = addBookingAndCommentResponseDto(userId, from, size);
        log.info("Список вещей пользователя с ид {} с номера {} размером {} возвращён.",
                userId, from, size);
        return itemGetResponseDtoList;
    }

    @Override
    public List<ItemDto> getItemsBySearch(String text, int from, int size) {
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Item> itemsPage = itemRepository.findAllBySearch(text, pageRequest);
        List<Item> items = itemsPage.getContent();
        List<ItemDto> itemsDto = ItemMapper.toItemDtos(items);
        log.info("Список свободных вещей по запросу \"{}\" с номера {} размером {} возвращён.",
                text, from, size);
        return itemsDto;
    }

    @Override
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        Item item = isItemPresent(itemId);
        User user = isUserPresent(userId);
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerId(itemId, userId).stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())
                        && booking.getStatus().equals(BookingStatus.APPROVED))
                .collect(Collectors.toList());
        isUserBooker(userId, itemId, bookings);
        Comment comment = new Comment(
                commentDto.getText(),
                item,
                user,
                LocalDateTime.now()
        );
        Comment savedComment = commentRepository.save(comment);
        log.info("Добавлен новый комментарий с ID = {}", savedComment.getId());
        return CommentMapper.toCommentDto(savedComment);
    }

    private List<ItemGetResponseDto> addBookingAndCommentResponseDto(long userId, int from, int size) {
        int page = from / size;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Item> itemsPage = itemRepository.findByOwnerId(userId, pageRequest);
        List<Item> items = itemsPage.getContent();
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());
        List<Booking> bookings = bookingRepository.findByItemIdIn(itemIds);
        if (bookings.isEmpty()) {
            return ItemMapper.toItemGetResponseDtos(items);
        }
        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);
        Map<Long, List<Booking>> itemIdToBookings = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(), Collectors.toList()));
        Map<Long, List<Comment>> itemIdToComments = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(), Collectors.toList()));
        List<ItemGetResponseDto> itemGetResponseDtoList = new ArrayList<>();
        for (Item item : items) {
            ItemGetResponseDto itemGetResponseDto = ItemMapper.toItemGetResponseDto(item);
            addBookingResponseDto(itemGetResponseDto, itemIdToBookings.getOrDefault(item.getId(), new ArrayList<>()));
            itemGetResponseDto.setComments(itemIdToComments.containsKey(item.getId()) ?
                    CommentMapper.toCommentDtos(itemIdToComments.get(item.getId())) : new ArrayList<>());
            itemGetResponseDtoList.add(itemGetResponseDto);
        }
        return itemGetResponseDtoList;
    }

    private void isUserBooker(long userId, long itemId, List<Booking> bookings) {
        if (bookings.isEmpty()) {
            log.error("Пользователь с ИД {} не может оставлять комментарии к веши с ИД {}.", userId, itemId);
            throw new NotValidException(String.format("Пользователь с ИД %d не может оставлять комментарии к веши с ИД %d.",
                    userId, itemId));
        }
    }

    private void addBookingResponseDto(ItemGetResponseDto itemGetResponseDto, List<Booking> bookings) {
        Optional<Booking> nextBooking = bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now())
                        && booking.getStatus().equals(BookingStatus.APPROVED))
                .min(Comparator.comparing(Booking::getStart));
        BookingResponseDto nextBookingResponseDto = nextBooking.map(BookingMapper::toBookingResponseDto).orElse(null);
        itemGetResponseDto.setNextBooking(nextBookingResponseDto);
        Optional<Booking> lastBooking = bookings.stream()
                .filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) &&
                        booking.getStatus().equals(BookingStatus.APPROVED))
                .max(Comparator.comparing(Booking::getEnd));
        BookingResponseDto lastBookingResponseDto = lastBooking.map(BookingMapper::toBookingResponseDto).orElse(null);
        itemGetResponseDto.setLastBooking(lastBookingResponseDto);
    }

    private void isUserOwner(Item item, long userId) {
        if (item.getOwner().getId() != userId) {
            log.error("Пользователь с ИД {} не является владельцем вещи с ИД {}.", userId, item.getId());
            throw new NotOwnerOrBookerException(String.format("Пользователь с ИД %d не является владельцем вещи с ИД %d.",
                    userId, item.getId()));
        }
    }

    private void isItemDtoValid(ItemDto itemDto) {
        if (itemDto.getName() == null
                || itemDto.getName().isBlank()
                || itemDto.getDescription() == null
                || itemDto.getDescription().isBlank()
                || itemDto.getAvailable() == null) {
            log.error("Вещь не прошла проверку.");
            throw new NotValidException("Вещь не прошла проверку.");
        }
    }

    private User isUserPresent(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.error("Пользователь с ИД {} отсутствует в БД.", userId);
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userId));
        }
        return optionalUser.get();
    }

    private Item isItemPresent(long itemId) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);
        if (optionalItem.isEmpty()) {
            log.error("Вещь с ИД {} отсутствует в БД.", itemId);
            throw new NotFoundException(String.format("Вещь с ИД %d отсутствует в БД.", itemId));
        }
        return optionalItem.get();
    }

    private ItemRequest isItemRequestPresent(long requestId) {
        Optional<ItemRequest> optionalItemRequest = itemRequestRepository.findById(requestId);
        if (optionalItemRequest.isEmpty()) {
            log.error("Запрос с ИД {} отсутствует в БД.", requestId);
            throw new NotFoundException(String.format("Запрос с ИД %d отсутствует в БД.", requestId));
        }
        return optionalItemRequest.get();
    }
}
