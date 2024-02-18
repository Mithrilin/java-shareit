package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {

    public static UserDto toUserDto(User user) {
        UserDto userDto = new UserDto(
                user.getName(),
                user.getEmail()
        );
        userDto.setId(user.getId());
        return userDto;
    }

    public static User toUser(UserDto userDto) {
        return new User(
                userDto.getName(),
                userDto.getEmail()
        );
    }
}
