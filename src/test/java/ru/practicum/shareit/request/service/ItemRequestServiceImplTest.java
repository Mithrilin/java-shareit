package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
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

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    private List<User> users = null;
    private List<Item> items = null;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @BeforeEach
    void setUp() {
        users = usersBuilder();
        items = itemBuilder();
    }

    @Test
    @DisplayName("Добавление запроса на вещь")
    void addItemRequest_whenUserPresent_thenItemRequestSaved() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemRequestDto itemRequestDto = new ItemRequestDto("описание запроса", null);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto returnedItemRequestDto = itemRequestService.addItemRequest(userId, itemRequestDto);

        assertEquals(itemRequestDto.getDescription(), returnedItemRequestDto.getDescription());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при добавлении запроса, когда пользователя нет в БД")
    void addItemRequest_whenUserNotPresent_thenThrowNotFoundException() {
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ItemRequestDto itemRequestDto = new ItemRequestDto("описание запроса", null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.addItemRequest(userId, itemRequestDto));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(itemRequestRepository, never()).save(Mockito.any());
    }

    @Test
    @DisplayName("Получение списка своих запросов, когда у пользователя нет запросов")
    void getAllItemRequestsByUserId_whenUserHaveNotRequests_thenReturnedEmptyList() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequestorIdOrderByIdDesc(userId)).thenReturn(new ArrayList<>());

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getAllItemRequestsByUserId(userId);

        assertTrue(itemRequestDtos.isEmpty());
    }

    @Test
    @DisplayName("Получение списка своих запросов, когда у пользователя 3 запроса")
    void getAllItemRequestsByUserId_whenUserHave3Requests_thenReturnedListWith3Requests() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemRequest itemRequest1 = new ItemRequest();
        ItemRequest itemRequest2 = new ItemRequest();
        ItemRequest itemRequest3 = new ItemRequest();
        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2, itemRequest3);
        when(itemRequestRepository.findByRequestorIdOrderByIdDesc(userId)).thenReturn(itemRequests);

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getAllItemRequestsByUserId(userId);

        assertEquals(3, itemRequestDtos.size());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при получении списка запросов пользователя, когда пользователя нет в БД")
    void getAllItemRequestsByUserId_whenUserNotPresent_thenThrowNotFoundException() {
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllItemRequestsByUserId(userId));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(itemRequestRepository, never()).findByRequestorIdOrderByIdDesc(userId);
    }

    @Test
    @DisplayName("Получение списка всех запросов, когда в БД 2 чужих запроса")
    void getAllItemRequests_whenTwoOtherUsersRequests_thenReturnedListWithTwoRequests() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ItemRequest itemRequest1 = new ItemRequest(2L, "описание 2", users.get(1), LocalDateTime.now().minusDays(2));
        ItemRequest itemRequest2 = new ItemRequest(3L, "описание 3", users.get(2), LocalDateTime.now().minusDays(1));
        Page<ItemRequest> itemRequestPage = new PageImpl<>(List.of(itemRequest1, itemRequest2));
        int from = 0;
        int size = 20;
        final String sortBy = "created";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.DESC, sortBy);
        when(itemRequestRepository.findByRequestorIdNot(userId, pageRequestParams.getPageRequest())).thenReturn(itemRequestPage);
        Item item1 = items.get(0);
        Item item2 = items.get(1);
        Item item3 = items.get(2);
        item1.setRequest(itemRequest1);
        item2.setRequest(itemRequest2);
        item3.setRequest(itemRequest1);
        List<Item> returnedItems = List.of(item1, item2, item3);
        when(itemRepository.findByRequestIdIn(any())).thenReturn(returnedItems);

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getAllItemRequests(userId, pageRequestParams);

        assertEquals(2, itemRequestDtos.size());
        assertEquals(itemRequest1.getDescription(), itemRequestDtos.get(0).getDescription());
        assertEquals(itemRequest2.getDescription(), itemRequestDtos.get(1).getDescription());
    }

    @Test
    @DisplayName("Получение списка всех запросов, когда в БД нет чужих запроса")
    void getAllItemRequests_whenHaveNotOtherUsersRequests_thenReturnedEmptyList() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Page<ItemRequest> itemRequestPage = Page.empty();
        int from = 0;
        int size = 20;
        final String sortBy = "created";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.DESC, sortBy);
        when(itemRequestRepository.findByRequestorIdNot(userId, pageRequestParams.getPageRequest())).thenReturn(itemRequestPage);

        List<ItemRequestDto> itemRequestDtos = itemRequestService.getAllItemRequests(userId, pageRequestParams);

        assertEquals(0, itemRequestDtos.size());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при получении списка всех запросов, когда пользователя нет в БД")
    void getAllItemRequests_whenUserNotPresent_thenThrowNotFoundException() {
        int from = 0;
        int size = 20;
        final String sortBy = "created";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.DESC, sortBy);
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getAllItemRequests(userId, pageRequestParams));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(itemRequestRepository, never()).findByRequestorIdNot(userId, pageRequestParams.getPageRequest());
    }

    @Test
    @DisplayName("Получение запроса по ид, когда в БД он есть")
    void getItemRequestById_whenItemRequestAndUserPresent_thenReturnedItemRequest() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        long itemRequestId = 1L;
        ItemRequest itemRequest = new ItemRequest(itemRequestId, "описание 1", user,
                LocalDateTime.now().minusDays(2));
        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.of(itemRequest));
        Item item1 = items.get(0);
        Item item2 = items.get(1);
        Item item3 = items.get(2);
        item1.setRequest(itemRequest);
        item2.setRequest(itemRequest);
        item3.setRequest(itemRequest);
        List<Item> returnedItems = List.of(item1, item2, item3);
        when(itemRepository.findByRequestId(itemRequestId)).thenReturn(returnedItems);
        List<ItemDto> itemDtos = ItemMapper.toItemDtos(returnedItems);

        ItemRequestDto itemRequestDto = itemRequestService.getItemRequestById(userId, itemRequestId);

        assertEquals(itemRequest.getDescription(), itemRequestDto.getDescription());
        assertEquals(3, itemRequestDto.getItems().size());
        assertEquals(itemDtos, itemRequestDto.getItems());
    }

    @Test
    @DisplayName("Ошибка NotFoundException при получении запроса по ид, когда в БД нет такого пользователя")
    void getItemRequestById_whenUserNotPresent_thenThrowNotFoundException() {
        User user = users.get(0);
        long userId = 1;
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        long itemRequestId = 1L;
        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, itemRequestId));

        assertEquals("Запрос с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(itemRepository, never()).findByRequestId(itemRequestId);
    }

    @Test
    @DisplayName("Ошибка NotFoundException при получении запроса по ид, когда в БД нет такого запроса")
    void getItemRequestById_whenItemRequestNotPresent_thenThrowNotFoundException() {
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        long itemRequestId = 1L;

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, itemRequestId));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(itemRequestRepository, never()).findById(itemRequestId);
        verify(itemRepository, never()).findByRequestId(itemRequestId);
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