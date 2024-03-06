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
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new ValidationException("У пользователя отсутствует ID");
        }
        User user = UserMapper.toUser(userDto);
        user = userRepository.save(user);
        userDto.setId(user.getId());
        log.info("Добавлен новый пользователь с ID = {}", user.getId());
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        Optional<User> optionalUser = userRepository.findById(userDto.getId());
        if (optionalUser.isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с ИД %d отсутствует в БД.", userDto.getId()));
        }
        User user = optionalUser.get();
        User newUser = UserMapper.toUser(userDto);
        User user1 = userRepository.save(UserMapper.toUser(userDto));

        log.info("Пользователь с ID {} обновлён.", user1.getId());
        return UserMapper.toUserDto(user1);
    }

    @Override
    public UserDto getUserById(long id) {
        User user = userRepository.getReferenceById(id);
        UserDto userDto = UserMapper.toUserDto(user);
        log.info("Пользователь с ID {} возвращён.", id);
        return userDto;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удалён.", id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        log.info("Текущее количество пользователей: {}. Список возвращён.", users.size());
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }
}
