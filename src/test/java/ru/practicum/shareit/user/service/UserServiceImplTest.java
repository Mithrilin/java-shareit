package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private static final String NAME_USER_ONE = "Name 111";
    private static final String NAME_USER_DTO_ONE = "DTO Name 111";
    private static final String NAME_USER_DTO_TWO = "DTO Name 222";
    private static final String EMAIL_USER_DTO_ONE = "DTO111@mail.ru";
    private static final String EMAIL_USER_DTO_TWO = "DTO222@mail.ru";
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

        NotValidException exception = assertThrows(
                NotValidException.class,
                () -> userServiceImpl.addUser(userDto));

        assertEquals("Email пользователя не прошёл валидацию.", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление пользователя с уже существующим Email")
    void testAddUser_ShouldThrowExceptionWhenEmailPresent() {
        userServiceImpl.addUser(userDtoOne);
        UserDto userDto = new UserDto(NAME_USER_ONE, userDtoOne.getEmail());

        NotValidException exception = assertThrows(
                NotValidException.class,
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

        assertEquals("new name", userDto2.getName());
    }

    @Test
    @DisplayName("Обновление имейла пользователя")
    void testUpdateUser_EmailsShouldBeEquals() {
        UserDto userDto1 = userServiceImpl.addUser(userDtoOne);
        UserDto userDtoNew = new UserDto(null, "new@mail.ru");
        userDtoNew.setId(userDto1.getId());

        UserDto userDto2 = userServiceImpl.updateUser(userDtoNew);

        assertEquals("new@mail.ru", userDto2.getEmail());
    }

    @Test
    @DisplayName("Обновление имейла на уже имеющийся в базе")
    void testUpdateUser_ShouldThrowExceptionWhenEmailPresent() {
        UserDto userDto1 = userServiceImpl.addUser(userDtoOne);
        userServiceImpl.addUser(userDtoTwo);
        UserDto userDtoNew = new UserDto(null, EMAIL_USER_DTO_TWO);
        userDtoNew.setId(userDto1.getId());

        NotValidException exception = assertThrows(
                NotValidException.class,
                () -> userServiceImpl.updateUser(userDtoNew));

        assertEquals(String.format("Пользователь с Email %s уже существует.", EMAIL_USER_DTO_TWO),
                exception.getMessage());
    }

    @Test
    @DisplayName("Обновление имейла если ид неправильный")
    void testUpdateUser_ShouldThrowExceptionWhenIdNotPresent() {
        userServiceImpl.addUser(userDtoOne);
        userServiceImpl.addUser(userDtoTwo);
        UserDto userDtoNew = new UserDto(null, EMAIL_USER_DTO_TWO);
        userDtoNew.setId(111L);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userServiceImpl.updateUser(userDtoNew));

        assertEquals(String.format("Пользователь с ИД %d отсутствует в БД.", 111L),
                exception.getMessage());
    }

    @Test
    @DisplayName("Получение пользователя по ид")
    void testGetUserById_ShouldBeEquals() {
        UserDto userDto1 = userServiceImpl.addUser(userDtoOne);

        UserDto userDtoNew = userServiceImpl.getUserById(userDto1.getId());

        assertEquals(userDto1.getName(), userDtoNew.getName());
        assertEquals(userDto1.getEmail(), userDtoNew.getEmail());
        assertEquals(userDto1.getId(), userDtoNew.getId());
    }

    @Test
    @DisplayName("Получение пользователя по неправильному ид")
    void testGetUserById_ShouldThrowExceptionWhenIdNotPresent() {
        userServiceImpl.addUser(userDtoOne);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userServiceImpl.getUserById(111L));

        assertEquals(String.format("Пользователь с ИД %d отсутствует в БД.", 111L),
                exception.getMessage());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser() {
        UserDto userDto1 = userServiceImpl.addUser(userDtoOne);
        userServiceImpl.addUser(userDtoTwo);

        userServiceImpl.deleteUser(userDto1.getId());

        List<UserDto> userDtos = userServiceImpl.getAllUsers();
        assertEquals(1, userDtos.size());
        assertEquals(2, userDtos.get(0).getId());
    }

    @Test
    @DisplayName("Удаление пользователя с неправильным ид")
    void testDeleteUser_ShouldThrowExceptionWhenIdNotPresent() {

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userServiceImpl.deleteUser(111L));

        assertEquals(String.format("Пользователь с ИД %d отсутствует в БД.", 111L),
                exception.getMessage());
    }

    @Test
    @DisplayName("Получение списка всех пользователей")
    void testGetAllUsers_ShouldBe2() {
        userServiceImpl.addUser(userDtoOne);
        userServiceImpl.addUser(userDtoTwo);

        List<UserDto> userDtos = userServiceImpl.getAllUsers();

        assertEquals(2, userDtos.size());
    }
}