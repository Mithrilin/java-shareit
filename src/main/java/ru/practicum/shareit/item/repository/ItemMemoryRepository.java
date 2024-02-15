package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemMemoryRepository implements ItemRepository {
    private final Map<Long, Item> itemMap = new HashMap<>();
    private long itemId = 1;

    @Override
    public Item addItem(Item item) {
        item.setId(itemId);
        itemMap.put(itemId, item);
        itemId++;
        return item;
    }

    @Override
    public Item getItemById(Long id) {
        if (!itemMap.containsKey(id)) {
            return null;
        }
        return itemMap.get(id);
    }

    @Override
    public List<Item> getAllItemsByUserId(long userId) {
        return itemMap.values().stream().filter(item -> item.getOwner().getId() == userId).collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsBySearch(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemMap.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text))
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }
}
