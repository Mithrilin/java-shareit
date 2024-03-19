package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private List<User> users = null;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeAll
    static void beforeAll() {

    }

    @BeforeEach
    void setUp() {
        users = usersBuilder();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("Возвращаем пользователя, когда он найден в БД.")
    void getUserById_whenUserFound_thenReturnedUser() {
        long userId = 1;
        User user = users.get(0);
        user.setId(userId);
        UserDto userDto = UserMapper.toUserDto(user);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto returnedUser = userService.getUserById(userId);

        assertEquals(userDto, returnedUser);
    }

    @Test
    @DisplayName("Ошибка NotFoundException, когда пользователь не найден в БД.")
    void getUserById_whenUserNotFound_thenThrowNotFoundException() {
        long userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getUserById(userId));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
    }

    @Test
    @DisplayName("Сохранение пользователя, когда пользователь валидный")
    void addUser_whenUserValid_thenUserSaved() {
        long userId = 1;
        User user = users.get(0);
        UserDto userDto = UserMapper.toUserDto(user);
        User savedUser = new User(user.getName(), user.getEmail());
        savedUser.setId(userId);
        when(userRepository.save(Mockito.any())).thenReturn(savedUser);

        UserDto returnedUserDto = userService.addUser(userDto);

        assertEquals(userDto.getName(), returnedUserDto.getName());
        assertEquals(userDto.getEmail(), returnedUserDto.getEmail());
    }

    @Test
    @DisplayName("Ошибка NotValidException, когда у пользователя нет почты")
    void addUser_whenUserNotValid_thenThrowNotValidException() {
        User user = users.get(0);
        user.setEmail(null);
        UserDto userDto = UserMapper.toUserDto(user);

        NotValidException exception = assertThrows(
                NotValidException.class,
                () -> userService.addUser(userDto));

        verify(userRepository, never()).save(Mockito.any());
        assertEquals("У пользователя отсутствует Email", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление пользователя, когда пользователь валидный")
    void updateUser_whenNewUserValid_thenUserUpdated() {
        long userId = 1;
        User oldUser = users.get(0);
        oldUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(oldUser));


        UserDto returnedUserDto = new UserDto("newUser", "newEmail");
        returnedUserDto.setId(userId);
//        when(userRepository.save(userId)).thenReturn(Optional.of(oldUser));

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
}