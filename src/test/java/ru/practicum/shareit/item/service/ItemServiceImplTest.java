package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private static final String NAME_ITEM_DTO_ONE = "DTO Name 111";
    private static final String NAME_ITEM_DTO_TWO = "DTO Name 222";
    private static final String DESCRIPTION_ITEM_DTO_ONE = "DTO description 111";
    private static final String DESCRIPTION_ITEM_DTO_TWO = "DTO description 222";
    private static final Boolean AVAILABLE_ITEM_DTO = true;
    private static final String NAME_USER_DTO_ONE = "DTO Name 111";
    private static final String EMAIL_USER_DTO_ONE = "DTO111@mail.ru";
    private static ItemDto itemDtoOne = null;
    private static ItemDto itemDtoTwo = null;
    private static UserDto userDtoOne = null;
    private final ItemServiceImpl itemServiceImpl;
    private final UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        itemDtoOne = new ItemDto(
                NAME_ITEM_DTO_ONE,
                DESCRIPTION_ITEM_DTO_ONE,
                AVAILABLE_ITEM_DTO,
                null
        );
        itemDtoTwo = new ItemDto(
                NAME_ITEM_DTO_TWO,
                DESCRIPTION_ITEM_DTO_TWO,
                AVAILABLE_ITEM_DTO,
                null
        );
        userDtoOne = new UserDto(
                NAME_USER_DTO_ONE,
                EMAIL_USER_DTO_ONE
        );
    }

    @Test
    @DisplayName("Добавление вещи с ид 1")
    void testAddItem_IdShouldBe1() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);

        ItemDto itemDto = itemServiceImpl.addItem(userDto.getId(), itemDtoOne);

        assertEquals(1, itemDto.getId());
    }

    @Test
    @DisplayName("Добавление вещи с нулловым именем")
    void testAddItem_ShouldThrowExceptionWhenNameNull() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = new ItemDto(null, DESCRIPTION_ITEM_DTO_ONE, AVAILABLE_ITEM_DTO, null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemServiceImpl.addItem(userDto.getId(), itemDto));

        assertEquals("Вещь не прошла проверку.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление вещи с пустым именем")
    void testAddItem_ShouldThrowExceptionWhenNameBlank() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = new ItemDto("", DESCRIPTION_ITEM_DTO_ONE, AVAILABLE_ITEM_DTO, null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemServiceImpl.addItem(userDto.getId(), itemDto));

        assertEquals("Вещь не прошла проверку.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление вещи с нулловым описанием")
    void testAddItem_ShouldThrowExceptionWhenDescriptionNull() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = new ItemDto(NAME_ITEM_DTO_ONE, null, AVAILABLE_ITEM_DTO, null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemServiceImpl.addItem(userDto.getId(), itemDto));

        assertEquals("Вещь не прошла проверку.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление вещи с пустым описанием")
    void testAddItem_ShouldThrowExceptionWhenDescriptionBlank() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = new ItemDto(NAME_ITEM_DTO_ONE, "", AVAILABLE_ITEM_DTO, null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemServiceImpl.addItem(userDto.getId(), itemDto));

        assertEquals("Вещь не прошла проверку.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление вещи с нулловым Available")
    void testAddItem_ShouldThrowExceptionWhenAvailableNull() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = new ItemDto(NAME_ITEM_DTO_ONE, DESCRIPTION_ITEM_DTO_ONE, null, null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> itemServiceImpl.addItem(userDto.getId(), itemDto));

        assertEquals("Вещь не прошла проверку.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление вещи с неправыильным ид пользователя")
    void testAddItem_ShouldThrowExceptionWhenUserIdNotPresent() {
        ItemDto itemDto = new ItemDto(NAME_ITEM_DTO_ONE, DESCRIPTION_ITEM_DTO_ONE, AVAILABLE_ITEM_DTO, null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemServiceImpl.addItem(111L, itemDto));

        assertEquals(String.format("Пользователь с ИД %d отсутствует в БД.", 111L), exception.getMessage());
    }

    @Test
    @DisplayName("Обновление вещи с нулловым именем")
    void testUpdateItem_ShouldBeEqualsWhenNameNull() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = itemServiceImpl.addItem(userDto.getId(), itemDtoOne);
        ItemDto newItemDto = new ItemDto(
                null,
                "newDescription",
                false,
                null
        );
        newItemDto.setId(itemDto.getId());

        ItemDto returnedItemDto = itemServiceImpl.updateItem(userDto.getId(), newItemDto);

        assertEquals(returnedItemDto.getName(), itemDto.getName());
        assertEquals(returnedItemDto.getDescription(), newItemDto.getDescription());
    }

    @Test
    @DisplayName("Обновление вещи с нулловым описанием")
    void testUpdateItem_ShouldBeEqualsWhenDescriptionNull() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = itemServiceImpl.addItem(userDto.getId(), itemDtoOne);
        ItemDto newItemDto = new ItemDto(
                "newName",
                null,
                false,
                null
        );
        newItemDto.setId(itemDto.getId());

        ItemDto returnedItemDto = itemServiceImpl.updateItem(userDto.getId(), newItemDto);

        assertEquals(returnedItemDto.getName(), newItemDto.getName());
        assertEquals(returnedItemDto.getDescription(), itemDto.getDescription());
    }

    @Test
    @DisplayName("Добавление вещи с неправыильным ид пользователя")
    void testUpdateItem_ShouldThrowExceptionWhenUserIdNotPresent() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        ItemDto itemDto = itemServiceImpl.addItem(userDto.getId(), itemDtoOne);
        ItemDto newItemDto = new ItemDto(
                "newName",
                null,
                false,
                null
        );
        newItemDto.setId(itemDto.getId());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemServiceImpl.updateItem(111L, newItemDto));

        assertEquals(String.format("Пользователь с ИД %d не является владельцем вещи с ИД %d.",
                111L, newItemDto.getId()), exception.getMessage());
    }

    @Test
    @DisplayName("Добавление вещи с неправыильным ид")
    void testUpdateItem_ShouldThrowExceptionWhenIdNotPresent() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        itemServiceImpl.addItem(userDto.getId(), itemDtoOne);
        ItemDto newItemDto = new ItemDto(
                "newName",
                null,
                false,
                null
        );
        newItemDto.setId(111L);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemServiceImpl.updateItem(userDto.getId(), newItemDto));

        assertEquals(String.format("Вещь с ИД %d отсутствует в БД.", 111L), exception.getMessage());
    }

    @Test
    @DisplayName("Получение вещи по неправильному ид")
    void testGetItemById_ShouldThrowExceptionWhenIdNotPresent() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        itemServiceImpl.addItem(userDto.getId(), itemDtoOne);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemServiceImpl.getItemById(1, 111L));

        assertEquals(String.format("Вещь с ИД %d отсутствует в БД.", 111L), exception.getMessage());
    }

    @Test
    @DisplayName("Получение всех вещей по неправильному ид пользователя")
    void testGetAllItemsByUserId_ShouldThrowExceptionWhenUserIdNotPresent() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        itemServiceImpl.addItem(userDto.getId(), itemDtoOne);
        itemServiceImpl.addItem(userDto.getId(), itemDtoTwo);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemServiceImpl.getAllItemsByUserId(111L));

        assertEquals(String.format("Пользователь с ИД %d отсутствует в БД.", 111L), exception.getMessage());
    }

    @Test
    @DisplayName("Получение всех вещей по неправильному ид пользователя")
    void testGetItemsBySearch_ShouldBeEquals() {
        UserDto userDto = userServiceImpl.addUser(userDtoOne);
        itemServiceImpl.addItem(userDto.getId(), itemDtoOne);
        itemServiceImpl.addItem(userDto.getId(), itemDtoTwo);

        List<ItemDto> itemDtos = itemServiceImpl.getItemsBySearch("dTo");

        assertEquals(2, itemDtos.size());
    }
}