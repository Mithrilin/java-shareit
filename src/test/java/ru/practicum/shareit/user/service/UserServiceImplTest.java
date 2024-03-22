package ru.practicum.shareit.user.service;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private List<User> users = null;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        users = usersBuilder();
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
    @DisplayName("Ошибка NotValidException при сохранении, когда у пользователя нет почты")
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
        User oldUser = users.get(0);
        long oldUserId = 1;
        oldUser.setId(oldUserId);
        when(userRepository.findById(oldUserId)).thenReturn(Optional.of(oldUser));
        UserDto userDto = new UserDto("new name", "newEmail@mail.ru");
        userDto.setId(oldUserId);
        User updatedUser = new User(userDto.getName(), userDto.getEmail());
        updatedUser.setId(oldUserId);
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserDto returnedUserDto = userService.updateUser(userDto);

        assertEquals(UserMapper.toUserDto(updatedUser), returnedUserDto);
    }

    @Test
    @DisplayName("Обновление пользователя, когда почта null")
    void updateUser_whenEmailIsNull_thenSavedOldEmail() {
        User oldUser = users.get(0);
        long oldUserId = 1;
        oldUser.setId(oldUserId);
        when(userRepository.findById(oldUserId)).thenReturn(Optional.of(oldUser));
        UserDto userDto = new UserDto("new name", null);
        userDto.setId(oldUserId);
        User updatedUser = new User();
        updatedUser.setId(oldUserId);
        updatedUser.setName(userDto.getName());
        updatedUser.setEmail(oldUser.getEmail());
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserDto returnedUserDto = userService.updateUser(userDto);

        assertEquals(UserMapper.toUserDto(updatedUser), returnedUserDto);
    }

    @Test
    @DisplayName("Обновление пользователя, когда имя null")
    void updateUser_whenNameIsNull_thenSavedOldName() {
        User oldUser = users.get(0);
        long oldUserId = 1;
        oldUser.setId(oldUserId);
        when(userRepository.findById(oldUserId)).thenReturn(Optional.of(oldUser));
        UserDto userDto = new UserDto(null, "newEmail@mail.ru");
        userDto.setId(oldUserId);
        User updatedUser = new User();
        updatedUser.setId(oldUserId);
        updatedUser.setName(oldUser.getName());
        updatedUser.setEmail(userDto.getEmail());
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserDto returnedUserDto = userService.updateUser(userDto);

        assertEquals(UserMapper.toUserDto(updatedUser), returnedUserDto);
    }

    @Test
    @DisplayName("Обновление пользователя, когда имя пустое")
    void updateUser_whenNameIsBlank_thenSavedOldName() {
        User oldUser = users.get(0);
        long oldUserId = 1;
        oldUser.setId(oldUserId);
        when(userRepository.findById(oldUserId)).thenReturn(Optional.of(oldUser));
        UserDto userDto = new UserDto("", "newEmail@mail.ru");
        userDto.setId(oldUserId);
        User updatedUser = new User();
        updatedUser.setId(oldUserId);
        updatedUser.setName(oldUser.getName());
        updatedUser.setEmail(userDto.getEmail());
        when(userRepository.save(any())).thenReturn(updatedUser);

        UserDto returnedUserDto = userService.updateUser(userDto);

        assertEquals(UserMapper.toUserDto(updatedUser), returnedUserDto);
    }

    @Test
    @DisplayName("Ошибка NotFoundException при обновлении пользователя, когда пользователя нет в БД")
    void updateUser_whenUserNotPresent_thenThrowNotFoundException() {
        long oldUserId = 1;
        when(userRepository.findById(oldUserId)).thenReturn(Optional.empty());
        UserDto userDto = new UserDto("new name", "newEmail@mail.ru");
        userDto.setId(oldUserId);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(userDto));

        assertEquals("Пользователь с ИД 1 отсутствует в БД.", exception.getMessage());
        verify(userRepository, never()).save(Mockito.any());
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
    @DisplayName("Удаление пользователя по ид")
    void deleteUserTest() {
        long oldUserId = 1;

        userService.deleteUser(oldUserId);

        verify(userRepository, times(1)).deleteById(oldUserId);
    }

    @Test
    @DisplayName("Возвращение списка всех пользователей, когда в БД 3 пользователя")
    void getAllUsers_when3Users_thenReturnedListWith3Users() {
        User user1 = users.get(0);
        Long user1Id = 1L;
        user1.setId(user1Id);
        User user2 = users.get(1);
        Long user2Id = 2L;
        user2.setId(user2Id);
        User user3 = users.get(2);
        Long user3Id = 3L;
        user3.setId(user3Id);
        List<User> returnedUsers = List.of(user1, user2, user3);
        when(userRepository.findAll()).thenReturn(returnedUsers);

        List<UserDto> allUsers = userService.getAllUsers();

        assertEquals(UserMapper.toUserDtos(returnedUsers), allUsers);
    }

    @Test
    @DisplayName("Возвращение пустого списка всех пользователей, когда БД пустая")
    void getAllUsers_whenEmptyDatabase_thenReturnedEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> allUsers = userService.getAllUsers();

        assertTrue(allUsers.isEmpty());
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