package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemGetResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.params.PageRequestParams;

import java.util.Collections;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable long id) {
        itemDto.setId(id);
        return itemService.updateItem(userId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemGetResponseDto getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PathVariable long id) {
        return itemService.getItemById(userId, id);
    }

    @GetMapping
    public List<ItemGetResponseDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                        @RequestParam int from,
                                                        @RequestParam int size) {
        final String sortBy = "id";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.ASC, sortBy);
        return itemService.getAllItemsByUserId(userId, pageRequestParams);
    }

    @GetMapping("/search")
    public List<ItemDto> getItemsBySearch(@RequestParam String text,
                                          @RequestParam int from,
                                          @RequestParam int size) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        final String sortBy = "id";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.ASC, sortBy);
        return itemService.getItemsBySearch(text, pageRequestParams);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}
