package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ItemRepositoryTest {
    private List<User> users = null;
    private List<Item> items = null;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        users = usersBuilder();
        items = itemBuilder();
    }

    @Test
    @DisplayName("Получение списка вещей по поисковому запросу, 1 совпадение")
    void findAllBySearch_when1Item_thenReturned1Item() {
        userRepository.save(users.get(0));
        userRepository.save(users.get(1));
        itemRepository.save(items.get(0));
        Item item = items.get(1);
        item.setName("Имя");
        itemRepository.save(item);
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));

        List<Item> items = itemRepository.findAllBySearch("Им", pageRequest).getContent();

        assertEquals(1, items.size());
        assertEquals(item.getName(), items.get(0).getName());
    }

    @Test
    @DisplayName("Получение пустого списка вещей по поисковому запросу, когда нет совпадений")
    void findAllBySearch_whenNoMatches_thenReturnedEmptyList() {
        userRepository.save(users.get(0));
        userRepository.save(users.get(1));
        itemRepository.save(items.get(0));
        itemRepository.save(items.get(1));
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));

        List<Item> items = itemRepository.findAllBySearch("Им", pageRequest).getContent();

        assertTrue(items.isEmpty());
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

    private List<Item> itemBuilder() {
        List<Item> items = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            Item item = new Item(
                    "name" + i,
                    "description" + i,
                    true
            );
            item.setOwner(users.get(i - 1));
            items.add(item);
        }
        return items;
    }
}