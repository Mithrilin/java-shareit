package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto returnedUser = userService.getUserById(userId);

        assertEquals(user.getName(), returnedUser.getName());
    }

    @Test
    @DisplayName("Ошибка NotFoundException, когда пользователь не найден в БД.")
    void getUserById_whenUserNotFound_thenThrowNotFoundException() {

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