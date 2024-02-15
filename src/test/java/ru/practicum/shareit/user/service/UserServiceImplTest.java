package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private static final String NAME_USER_ONE = "Name 111";
    private static final String NAME_USER_DTO_ONE = "DTO Name 111";
    private static final String NAME_USER_DTO_TWO = "DTO Name 222";
    private static final String EMAIL_USER_ONE = "111@mail.ru";
    private static final String EMAIL_USER_DTO_ONE = "DTO111@mail.ru";
    private static final String EMAIL_USER_DTO_TWO = "DTO222@mail.ru";
    private static User userOne = null;
    private static UserDto userDtoOne = null;
    private static UserDto userDtoTwo = null;
    private final UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        userDtoOne = new UserDto(NAME_USER_DTO_ONE, EMAIL_USER_DTO_ONE);
        userDtoTwo = new UserDto(NAME_USER_DTO_TWO, EMAIL_USER_DTO_TWO);
    }

    @Test
    @DisplayName("Добавление пользователя с ид 1")
    void testAddUser_IdShouldBe1() {
        UserDto returnedUserDto = userServiceImpl.addUser(userDtoOne);

        assertEquals(1, returnedUserDto.getId());
    }

    @Test
    @DisplayName("Добавление пользователя без Email")
    void testAddUser_ShouldThrowExceptionWhenEmailNull() {
        UserDto userDto = new UserDto(NAME_USER_DTO_ONE, null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userServiceImpl.addUser(userDto));

        assertEquals("Email пользователя не должен быть null.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление пользователя с пустым Email")
    void testAddUser_ShouldThrowExceptionWhenEmailBlank() {
        UserDto userDto = new UserDto(NAME_USER_DTO_ONE, "");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userServiceImpl.addUser(userDto));

        assertEquals("Email пользователя не прошёл валидацию.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление пользователя с уже существующим Email")
    void testAddUser_ShouldThrowExceptionWhenEmailPresent() {
        userServiceImpl.addUser(userDtoOne);
        UserDto userDto = new UserDto(NAME_USER_ONE, userDtoOne.getEmail());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userServiceImpl.addUser(userDto));

        assertEquals("Email пользователя не прошёл валидацию.", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление имени пользователя")
    void testUpdateUser_NamesShouldBeEquals() {
        UserDto userDto1 = userServiceImpl.addUser(userDtoOne);
        UserDto userDtoNew = new UserDto("new name", null);
        userDtoNew.setId(userDto1.getId());

        UserDto userDto2 = userServiceImpl.updateUser(userDtoNew);

        assertEquals(userDtoOne.getEmail(), userDto2.getEmail());
    }

    @Test
    void getUserById() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void getAllUsers() {
    }
}