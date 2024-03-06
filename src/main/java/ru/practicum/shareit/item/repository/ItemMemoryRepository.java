package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

//@Repository
public class ItemMemoryRepository {
//    private final Map<Long, Item> itemMap = new HashMap<>();
//    private final Map<Long, List<Item>> userItemIndex = new LinkedHashMap<>();
//    private long itemId = 1;
//
//
//    public Item addItem(Item item) {
//        final List<Item> items = userItemIndex.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>());
//        item.setId(itemId);
//        itemMap.put(itemId, item);
//        itemId++;
//        items.add(item);
//        return item;
//    }
//
//
//    public Item getItemById(Long id) {
//        if (!itemMap.containsKey(id)) {
//            return null;
//        }
//        return itemMap.get(id);
//    }
//
//
//    public List<Item> getAllItemsByUserId(long userId) {
//        return userItemIndex.get(userId);
//    }
//
//
//    public List<Item> getItemsBySearch(String text) {
//        if (text.isBlank()) {
//            return new ArrayList<>();
//        }
//        return itemMap.values().stream()
//                .filter(item -> item.getName().toLowerCase().contains(text)
//                        || item.getDescription().toLowerCase().contains(text))
//                .filter(Item::getAvailable)
//                .collect(Collectors.toList());
//    }
}
