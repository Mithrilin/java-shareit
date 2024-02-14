package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {

    User addUser(User user);

    boolean isEmailPresent(String userEmail);

    User getUserById(Long id);

    void changeEmailInMap(String newEmail, String oldEmail);

    void deleteUser(long id);

    List<User> getAllUsers();
}
