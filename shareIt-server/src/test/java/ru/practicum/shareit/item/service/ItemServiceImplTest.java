package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerOrBookerException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemGetResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.params.PageRequestParams;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.BookingStatus.APPROVED;
import static ru.practicum.shareit.booking.BookingStatus.WAITING;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    private List<User> users = null;
    private List<Item> items = null;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        users = usersBuilder();
        items = itemBuilder();
    }

    @Test
    @DisplayName("Успешное добавление вещи, когда requestId = null")
    void addItem_whenRequestIdIsNull_thenReturnedItemDto() {
        long userId = 1;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemDto itemDto = ItemMapper.toItemDto(items.get(0));
        Item item = ItemMapper.toItem(itemDto);
        item.setId(1L);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto returnedItemDto = itemService.addItem(userId, itemDto);

        assertEquals(itemDto.getName(), returnedItemDto.getName());
        assertEquals(itemDto.getDescription(), returnedItemDto.getDescription());
    }

    @Test
    @DisplayName("Успешное добавление вещи, когда requestId не null")
    void addItem_whenRequestIdNotNull_thenReturnedItemDto() {
        long userId = 1;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Item item = items.get(0);
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        item.setRequest(itemRequest);
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        ItemDto itemDto = ItemMapper.toItemDto(item);
        Item itemForReturn = ItemMapper.toItem(itemDto);
        itemForReturn.setId(1L);
        when(itemRepository.save(any(Item.class))).thenReturn(itemForReturn);

        ItemDto returnedItemDto = itemService.addItem(userId, itemDto);

        assertEquals(itemDto.getName(), returnedItemDto.getName());
        assertEquals(itemDto.getDescription(), returnedItemDto.getDescription());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при добавление вещи, когда в БД нет запроса с таким ид")
    void addItem_whenItemRequestNotPresent_thenThrowNotFoundException() {
        long userId = 1;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Item item = items.get(0);
        ItemRequest itemRequest = new ItemRequest(1L, "описание", user, LocalDateTime.now());
        item.setRequest(itemRequest);
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.empty());
        ItemDto itemDto = ItemMapper.toItemDto(item);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.addItem(userId, itemDto));

        assertEquals("Запрос с ИД 1 отсутствует в БД.", exception.getMessage());

        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при добавление вещи, когда в БД нет пользователя с таким ид")
    void addItem_whenUserNotPresent_thenThrowNotFoundException() {
        long userId = 1;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Item item = items.get(0);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.addItem(userId, itemDto));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());

        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное обновление вещи")
    void updateItem_whenItemDtoIsValid_thenReturnedItemDto() {
        long userId = 1L;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemDto itemDto = new ItemDto("new nsme", "new description", false, null);
        long itemDtoId = 1L;
        itemDto.setId(itemDtoId);
        Item item = items.get(0);
        item.setId(itemDtoId);
        when(itemRepository.findById(itemDtoId)).thenReturn(Optional.of(item));
        Item updatedItem = new Item(
                item.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                item.getOwner(),
                item.getRequest()
        );
        when(itemRepository.save(any())).thenReturn(updatedItem);

        ItemDto returnedItemDto = itemService.updateItem(userId, itemDto);

        assertEquals(ItemMapper.toItemDto(updatedItem), returnedItemDto);
    }

    @Test
    @DisplayName("Успешное обновление вещи, когда обновлять нечего")
    void updateItem_whenItemDtoIsNotValid_thenReturnedItemDto() {
        long userId = 1L;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemDto itemDto = new ItemDto(null, null, null, null);
        long itemDtoId = 1L;
        itemDto.setId(itemDtoId);
        Item item = items.get(0);
        item.setId(itemDtoId);
        when(itemRepository.findById(itemDtoId)).thenReturn(Optional.of(item));
        Item updatedItem = new Item(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner(),
                item.getRequest()
        );
        when(itemRepository.save(any())).thenReturn(updatedItem);

        ItemDto returnedItemDto = itemService.updateItem(userId, itemDto);

        assertEquals(ItemMapper.toItemDto(updatedItem), returnedItemDto);
    }

    @Test
    @DisplayName("Ошибка NotFoundException при обновлении вещи, когда в БД нет вещи с таким ид")
    void updateItem_whenItemNotPresent_thenThrowNotFoundException() {
        long userId = 1L;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemDto itemDto = new ItemDto("new nsme", "new description", false, null);
        long itemDtoId = 1L;
        itemDto.setId(itemDtoId);
        when(itemRepository.findById(itemDtoId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(userId, itemDto));

        assertEquals("Вещь с ИД 1 отсутствует в БД.", exception.getMessage());

        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка NotOwnerOrBookerException при обновлении вещи, когда пользователь не является владельцем вещи")
    void updateItem_whenUserNotOwner_thenThrowNotOwnerOrBookerException() {
        long userId = 1L;
        User user = users.get(0);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemDto itemDto = new ItemDto("new nsme", "new description", false, null);
        long itemDtoId = 1L;
        itemDto.setId(itemDtoId);
        User user1 = users.get(1);
        user1.setId(2L);
        Item item = items.get(1);
        item.setId(itemDtoId);
        when(itemRepository.findById(itemDtoId)).thenReturn(Optional.of(item));

        NotOwnerOrBookerException exception = assertThrows(
                NotOwnerOrBookerException.class,
                () -> itemService.updateItem(userId, itemDto));

        assertEquals("Пользователь с ИД 1 не является владельцем вещи с ИД 1.", exception.getMessage());

        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Успешное получение вещи по ид, когда пользователь не владелец")
    void getItemById_whenUserNotOwner_thenReturnedItemDto() {
        long itemId = 1L;
        long userId = 1L;
        User user = users.get(0);
        user.setId(userId);
        Item item = items.get(0);
        item.setId(itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findByItemId(itemId)).thenReturn(new ArrayList<>());
        when(commentRepository.findByItemId(itemId)).thenReturn(new ArrayList<>());

        ItemGetResponseDto returnedItemGetResponseDto = itemService.getItemById(userId, itemId);

        assertEquals(item.getName(), returnedItemGetResponseDto.getName());
        assertEquals(item.getDescription(), returnedItemGetResponseDto.getDescription());
        assertTrue(returnedItemGetResponseDto.getComments().isEmpty());
    }

    @Test
    @DisplayName("Получение списка всех вещей владельцем, когда в БД 2 вещи")
    void getAllItemsByUserId_when2Items_thenReturnedItemGetResponseDtoList() {
        long user1Id = 1L;
        long user2Id = 2L;
        User user1 = users.get(0);
        User user2 = users.get(1);
        user1.setId(user1Id);
        user2.setId(user2Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        long itemId1 = 1L;
        long itemId2 = 2L;
        Item item1 = items.get(0);
        Item item2 = items.get(2);
        item2.setOwner(user1);
        item1.setId(itemId1);
        item2.setId(itemId2);
        Page<Item> itemsPage = new PageImpl<>(List.of(item1, item2));
        when(itemRepository.findByOwnerId(user1Id, pageRequest)).thenReturn(itemsPage);
        Booking booking1 = new Booking(1L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2), item1,
                user2, WAITING);
        Booking booking2 = new Booking(2L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2), item2,
                user2, WAITING);
        List<Booking> bookings = List.of(booking1, booking2);
        when(bookingRepository.findByItemIdIn(List.of(itemId1, itemId2))).thenReturn(bookings);
        when(commentRepository.findByItemIdIn(List.of(itemId1, itemId2))).thenReturn(new ArrayList<>());

        List<ItemGetResponseDto> itemGetResponseDtoList = itemService.getAllItemsByUserId(user1Id,
                new PageRequestParams(0, 20, Sort.Direction.ASC, "id"));

        assertEquals(2, itemGetResponseDtoList.size());
        assertEquals(item1.getName(), itemGetResponseDtoList.get(0).getName());
        assertEquals(item1.getDescription(), itemGetResponseDtoList.get(0).getDescription());
        assertEquals(item2.getName(), itemGetResponseDtoList.get(1).getName());
        assertEquals(item2.getDescription(), itemGetResponseDtoList.get(1).getDescription());
    }

    @Test
    @DisplayName("Получение списка всех вещей владельцем, когда у вещей нет бронирований")
    void getAllItemsByUserId_when2ItemsWithoutBooking_thenReturnedItemGetResponseDtoList() {
        long user1Id = 1L;
        long user2Id = 2L;
        User user1 = users.get(0);
        User user2 = users.get(1);
        user1.setId(user1Id);
        user2.setId(user2Id);
        when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        long itemId1 = 1L;
        long itemId2 = 2L;
        Item item1 = items.get(0);
        Item item2 = items.get(2);
        item2.setOwner(user1);
        item1.setId(itemId1);
        item2.setId(itemId2);
        Page<Item> itemsPage = new PageImpl<>(List.of(item1, item2));
        when(itemRepository.findByOwnerId(user1Id, pageRequest)).thenReturn(itemsPage);
        when(bookingRepository.findByItemIdIn(List.of(itemId1, itemId2))).thenReturn(new ArrayList<>());

        List<ItemGetResponseDto> itemGetResponseDtoList = itemService.getAllItemsByUserId(user1Id,
                new PageRequestParams(0, 20, Sort.Direction.ASC, "id"));

        verify(commentRepository, never()).findByItemIdIn(any());
        assertEquals(2, itemGetResponseDtoList.size());
        assertEquals(item1.getName(), itemGetResponseDtoList.get(0).getName());
        assertEquals(item1.getDescription(), itemGetResponseDtoList.get(0).getDescription());
        assertEquals(item2.getName(), itemGetResponseDtoList.get(1).getName());
        assertEquals(item2.getDescription(), itemGetResponseDtoList.get(1).getDescription());
    }

    @Test
    @DisplayName("Получение списка вещей по поисковому запросу, когда в БД 2 вещь")
    void getItemsBySearch_when2Items_thenReturnedItemDtoList() {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
        long itemId1 = 1L;
        long itemId2 = 2L;
        Item item1 = items.get(0);
        Item item2 = items.get(1);
        item1.setId(itemId1);
        item2.setId(itemId2);
        Page<Item> itemsPage = new PageImpl<>(List.of(item1, item2));
        when(itemRepository.findAllBySearch("text", pageRequest)).thenReturn(itemsPage);

        List<ItemDto> returnedItemDtos = itemService.getItemsBySearch("text",
                new PageRequestParams(0, 20, Sort.Direction.ASC, "id"));

        assertEquals(2, returnedItemDtos.size());
        assertEquals(item1.getName(), returnedItemDtos.get(0).getName());
        assertEquals(item1.getDescription(), returnedItemDtos.get(0).getDescription());
        assertEquals(item2.getName(), returnedItemDtos.get(1).getName());
        assertEquals(item2.getDescription(), returnedItemDtos.get(1).getDescription());
    }

    @Test
    @DisplayName("Добавление комментария к вещи, когда пользователь арендатор")
    void addComment_whenUserIsBooker_thenReturnedCommentDto() {
        long itemId = 1L;
        Item item = items.get(1);
        item.setId(itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        long userId = 1L;
        User user = users.get(1);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Booking booking = new Booking(1L, LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2), item,
                user, APPROVED);
        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findByItemIdAndBookerId(itemId, userId)).thenReturn(bookings);
        CommentDto commentDto = new CommentDto(null, "text", user.getName(), LocalDateTime.now());
        Comment comment = new Comment(commentDto.getText(), item, user, LocalDateTime.now());
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto savedCommentDto = itemService.addComment(userId, itemId, commentDto);

        assertEquals(commentDto.getText(), savedCommentDto.getText());
        assertEquals(commentDto.getAuthorName(), savedCommentDto.getAuthorName());
    }

    @Test
    @DisplayName("Ошибка NotValidException при добавлении комментария к вещи, когда пользователь не арендатор")
    void addComment_whenUserNotBooker_thenThrowNotValidException() {
        long itemId = 1L;
        Item item = items.get(1);
        item.setId(itemId);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        long userId = 1L;
        User user = users.get(1);
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemIdAndBookerId(itemId, userId)).thenReturn(new ArrayList<>());
        CommentDto commentDto = new CommentDto(null, "text", user.getName(), LocalDateTime.now());

        NotValidException exception = assertThrows(
                NotValidException.class,
                () -> itemService.addComment(userId, itemId, commentDto));

        assertEquals("Пользователь с ИД 1 не может оставлять комментарии к веши с ИД 1.", exception.getMessage());

        verify(commentRepository, never()).save(any());
    }

    private List<User> usersBuilder() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            users.add(
                    new User(
                            "name" + i,
                            "name" + i + "@mail.ru"
                    )
            );
        }
        return users;
    }

    private List<Item> itemBuilder() {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            Item item = new Item(
                    "name" + i,
                    "description" + i,
                    true
            );
            item.setOwner(users.get(i - 1));
            items.add(item);
        }
        return items;
    }
}