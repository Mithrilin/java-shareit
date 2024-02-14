package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new RuntimeException("Email пользователя не должен быть null.");
        }
        if (userDto.getEmail().isBlank() || userRepository.isEmailPresent(userDto.getEmail())) {
            throw new ValidationException("Email пользователя не прошёл валидацию.");
        }
        User user = UserMapper.toUser(userDto);
        user = userRepository.addUser(user);
        userDto.setId(user.getId());
        log.info("Добавлен новый пользователь с ID = {}", user.getId());
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = userRepository.getUserById(userDto.getId());
        if (user == null) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userDto.getId()));
        }
        String oldEmail = user.getEmail();
        String newEmail = userDto.getEmail();
        if (newEmail != null) {
            if (userRepository.isEmailPresent(newEmail) && !oldEmail.equals(newEmail)) {
                throw new ValidationException(String.format("Пользователь с Email %s уже существует.", newEmail));
            }
            userRepository.changeEmailInMap(newEmail, oldEmail);
            user.setEmail(newEmail);
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        log.info("Пользователь с ID {} обновлён.", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(long id) {
        User user = userRepository.getUserById(id);
        UserDto userDto = UserMapper.toUserDto(user);
        log.info("Пользователь с ID {} возвращён.", id);
        return userDto;
    }

    @Override
    public void deleteUser(long id) {
        if (userRepository.getUserById(id) == null) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", id));
        }
        userRepository.deleteUser(id);
        log.info("Пользователь с ID {} удалён.", id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.getAllUsers();
        log.info("Текущее количество пользователей: {}. Список возвращён.", users.size());
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
