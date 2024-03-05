package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserMemoryRepository {
    private final Map<Long, User> userMap = new HashMap<>();
    private final Map<String, User> usersEmailMap = new HashMap<>();
    private long userId = 1;

    public User addUser(User user) {
        user.setId(userId);
        userMap.put(userId, user);
        userId++;
        usersEmailMap.put(user.getEmail(), user);
        return user;
    }

    public User getUserById(Long id) {
        if (!userMap.containsKey(id)) {
            return null;
        }
        return userMap.get(id);
    }

    public void changeEmailInMap(String newEmail, String oldEmail) {
        User user = usersEmailMap.get(oldEmail);
        usersEmailMap.remove(oldEmail);
        usersEmailMap.put(newEmail, user);
    }

    public void deleteUser(long id) {
        User user = userMap.get(id);
        usersEmailMap.remove(user.getEmail());
        userMap.remove(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }
}
