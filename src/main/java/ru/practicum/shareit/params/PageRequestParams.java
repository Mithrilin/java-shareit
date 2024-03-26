package ru.practicum.shareit.params;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@Data
public class PageRequestParams {
    private Integer from;
    private Integer size;
    private Integer page;
    private Sort sort;
    private PageRequest pageRequest;

    public PageRequestParams(Integer from, Integer size, Direction direction, String sortBy) {
        this.from = from;
        this.size = size;
        this.page = from / size;
        this.sort = Sort.by(direction, sortBy);
        this.pageRequest = PageRequest.of(page, size, sort);
    }
}
