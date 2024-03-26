package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.params.PageRequestParams;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.BookingStatus.WAITING;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BookingService bookingService;

    @Test
    @DisplayName("Успешное создание бронирования")
    void createBooking_whenBookingDtoIsValid_thenReturnedBookingDto() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, WAITING);
        when(bookingService.addBooking(Mockito.anyLong(), Mockito.any(BookingDto.class))).thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Успешное подтверждение бронирования")
    void approveBooking_whenBookingIdIsPositive_thenReturnedBookingDto() throws Exception {
        BookingDto bookingDto = new BookingDto(1L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, WAITING);
        when(bookingService.approveBooking(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(bookingDto);

        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Получение списка бронирований пользователем, создавшим бронирования")
    void getAllBookingByBookerId_when2BookingDto_thenReturnedBookingDtoList() throws Exception {
        BookingDto bookingDto1 = new BookingDto(1L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, WAITING);
        BookingDto bookingDto2 = new BookingDto(2L, 2L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3),
                null, null, WAITING);
        List<BookingDto> bookingDtos = List.of(bookingDto1, bookingDto2);
        when(bookingService.getAllBookingByBookerId(Mockito.anyLong(), Mockito.anyString(), Mockito.any(PageRequestParams.class)))
                .thenReturn(bookingDtos);

        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(bookingDto2.getStatus().toString())));
    }

    @Test
    @DisplayName("Получение списка бронирований владельцем вещей")
    void getAllBookingByOwnerId_when2BookingDto_thenReturnedBookingDtoList() throws Exception {
        BookingDto bookingDto1 = new BookingDto(1L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, WAITING);
        BookingDto bookingDto2 = new BookingDto(2L, 2L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3),
                null, null, WAITING);
        List<BookingDto> bookingDtos = List.of(bookingDto1, bookingDto2);
        when(bookingService.getAllBookingByOwnerId(Mockito.anyLong(), Mockito.anyString(), Mockito.any(PageRequestParams.class)))
                .thenReturn(bookingDtos);

        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookingDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto1.getStatus().toString())))
                .andExpect(jsonPath("$[1].id", is(bookingDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(bookingDto2.getStatus().toString())));
    }
}