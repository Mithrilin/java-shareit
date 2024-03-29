package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.params.PageRequestParams;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") long userId,
                                            @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllItemRequestsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getAllItemRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @RequestParam int from,
                                                   @RequestParam int size) {
        final String sortBy = "created";
        final PageRequestParams pageRequestParams = new PageRequestParams(from, size, Sort.Direction.DESC, sortBy);
        return itemRequestService.getAllItemRequests(userId, pageRequestParams);
    }

    @GetMapping("/{itemRequestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long itemRequestId) {
        return itemRequestService.getItemRequestById(userId, itemRequestId);
    }
}
