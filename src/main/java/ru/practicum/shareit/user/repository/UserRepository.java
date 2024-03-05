package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

//    User addUser(User user);
//
//    boolean isEmailPresent(String userEmail);
//
//    User getUserById(Long id);
//
//    void changeEmailInMap(String newEmail, String oldEmail);
//
//    void deleteUser(long id);
//
//    List<User> getAllUsers();
}
