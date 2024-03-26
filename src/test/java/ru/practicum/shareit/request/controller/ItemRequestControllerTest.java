package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.params.PageRequestParams;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    @DisplayName("Успешное сохранение запроса")
    void createItemRequest_whenItemRequestDtoIsValid_thenReturnedItemRequestDto() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto("описание", null);
        itemRequestDto.setId(1L);
        when(itemRequestService.addItemRequest(Mockito.anyLong(), Mockito.any(ItemRequestDto.class))).thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }

    @Test
    @DisplayName("Получение списка запросов пользователя")
    void getAllItemRequestsByUserId_when2ItemRequestDto_thenReturnedItemRequestDtoList() throws Exception {
        List<ItemRequestDto> itemRequestDtos = List.of(new ItemRequestDto("описание", null),
                new ItemRequestDto("описание2", null));
        when(itemRequestService.getAllItemRequestsByUserId(Mockito.anyLong())).thenReturn(itemRequestDtos);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[1].description", is(itemRequestDtos.get(1).getDescription())));
    }

    @Test
    @DisplayName("Получение списка запросов")
    void getAllItemRequests_when2ItemRequestDto_thenReturnedItemRequestDtoList() throws Exception {
        List<ItemRequestDto> itemRequestDtos = List.of(new ItemRequestDto("описание", null),
                new ItemRequestDto("описание2", null));
        when(itemRequestService.getAllItemRequests(Mockito.anyLong(), Mockito.any(PageRequestParams.class)))
                .thenReturn(itemRequestDtos);

        mvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "20")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is(itemRequestDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[1].description", is(itemRequestDtos.get(1).getDescription())));
    }

    @Test
    @DisplayName("Успешное получение запроса по ид")
    void getItemRequestById_whenItemRequestIdIsPositive_thenReturnedItemRequestDto() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto("описание", null);
        itemRequestDto.setId(1L);
        when(itemRequestService.getItemRequestById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(itemRequestDto);

        mvc.perform(get("/requests/{itemRequestId}", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }
}