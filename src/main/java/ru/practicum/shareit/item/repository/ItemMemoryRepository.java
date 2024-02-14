package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.Map;

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
}
