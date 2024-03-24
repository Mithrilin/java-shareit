package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemGetResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemService itemService;

    @Test
    @DisplayName("Успешное сохранение вещи")
    void createItem_whenItemDtoIsValid_thenReturnedItemDto() throws Exception {
        ItemDto itemDto = new ItemDto("name", "description", true, null);
        itemDto.setId(1L);
        when(itemService.addItem(Mockito.anyLong(), Mockito.any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
    }

    @Test
    @DisplayName("Успешное обновление вещи")
    void updateItem_whenItemDtoIsValid_thenReturnedItemDto() throws Exception {
        ItemDto itemDto = new ItemDto("name", "description", true, null);
        itemDto.setId(1L);
        when(itemService.updateItem(Mockito.anyLong(), Mockito.any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(patch("/items/{id}", 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
    }

    @Test
    @DisplayName("Успешное получение вещи по ид")
    void getItemById_whenIdIsPositive_thenReturnedItemGetResponseDto() throws Exception {
        ItemGetResponseDto itemGetResponseDto = new ItemGetResponseDto(1L, "name", "description", true);
        when(itemService.getItemById(Mockito.anyLong(), Mockito.anyLong())).thenReturn(itemGetResponseDto);

        mvc.perform(get("/items/{id}", 1)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemGetResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemGetResponseDto.getName())))
                .andExpect(jsonPath("$.description", is(itemGetResponseDto.getDescription())));
    }

    @Test
    @DisplayName("Получение списка вещей пользователя")
    void getAllItemsByUserId_whenIdIsPositive_thenReturnedItemGetResponseDtoList() throws Exception {
        List<ItemGetResponseDto> itemGetResponseDtos = List.of(
                new ItemGetResponseDto(1L, "name1", "description1", true),
                new ItemGetResponseDto(2L, "name2", "description2", true));
        when(itemService.getAllItemsByUserId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(itemGetResponseDtos);

        mvc.perform(get("/items")
                        .param("from", "0")
                        .param("size", "20")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemGetResponseDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemGetResponseDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(itemGetResponseDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[1].id", is(itemGetResponseDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemGetResponseDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].description", is(itemGetResponseDtos.get(1).getDescription())));
    }

    @Test
    @DisplayName("Получение списка вещей по поисковому запросу")
    void getItemsBySearch_whenTextNotNullAndNotBlank_thenReturnedItemDtoList() throws Exception {
        List<ItemDto> itemDtos = List.of(
                new ItemDto("name1", "description1", true, null),
                new ItemDto("name2", "description2", true, null)
        );
        when(itemService.getItemsBySearch(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(itemDtos);

        mvc.perform(get("/items/search")
                        .param("text", "name")
                        .param("from", "0")
                        .param("size", "20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(itemDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtos.get(0).getName())))
                .andExpect(jsonPath("$[0].description", is(itemDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[1].id", is(itemDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemDtos.get(1).getName())))
                .andExpect(jsonPath("$[1].description", is(itemDtos.get(1).getDescription())));
    }

    @Test
    @DisplayName("Получение пустого списка вещей по поисковому запросу")
    void getItemsBySearch_whenTextIsBlank_thenReturnedEmptyItemDtoList() throws Exception {

        mvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "20")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService, never()).getItemsBySearch(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    @DisplayName("Успешное добавление отзыва к вещи")
    void createComment_whenCommentDtoIsValid_thenReturnedCommentDto() throws Exception {
        CommentDto commentDto = new CommentDto(1L, "text", "name", null);
        when(itemService.addComment(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(CommentDto.class))).thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }
}