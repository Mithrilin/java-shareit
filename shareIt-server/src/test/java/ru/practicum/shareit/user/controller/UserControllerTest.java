package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Успешное сохранение пользователя")
    void createUser_whenUserDtoValid_thenReturnedUserDto() throws Exception {
        UserDto userDto = new UserDto("name", "email@mail.ru");
        userDto.setId(1L);
        when(userService.addUser(Mockito.any(UserDto.class))).thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    @DisplayName("Успешное обновление пользователя")
    void updateUser_whenUserDtoIsValidAndIdIsPositive_thenReturnedUserDto() throws Exception {
        UserDto userDto = new UserDto("name", "email@mail.ru");
        userDto.setId(1L);
        when(userService.updateUser(Mockito.any(UserDto.class))).thenReturn(userDto);

        mvc.perform(patch("/users/{id}", 1)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    @DisplayName("Успешное получение пользователя по ид")
    void getUserById_whenIdIsPositive_thenReturnedUserDto() throws Exception {
        UserDto userDto = new UserDto("name", "email@mail.ru");
        userDto.setId(1L);
        when(userService.getUserById(Mockito.anyLong())).thenReturn(userDto);

        mvc.perform(get("/users/{id}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    @DisplayName("Успешное удаление пользователя по ид")
    void deleteUser_whenIdIsPositive_thenReturnedUserDto() throws Exception {
        UserDto userDto = new UserDto("name", "email@mail.ru");
        userDto.setId(1L);

        mvc.perform(delete("/users/{id}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService).deleteUser(Mockito.anyLong());
    }

    @Test
    @DisplayName("Успешное получение списка всех пользователей")
    void findAllUsers_whenIdIsPositive_thenReturnedUserDto() throws Exception {
        UserDto userDto = new UserDto("name", "email@mail.ru");
        userDto.setId(1L);
        List<UserDto> userDtos = List.of(userDto, new UserDto("name2", "email2@mail.ru"));
        userDtos.get(1).setId(2L);
        when(userService.getAllUsers()).thenReturn(userDtos);

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}