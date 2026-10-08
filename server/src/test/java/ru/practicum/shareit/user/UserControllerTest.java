package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;


import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    public void createUserTest() throws Exception {
        UserDto inputDto = createUserDto(null, "Ivan Ivanov", "ivan@example.com");
        UserDto outputDto = createUserDto(1L, "Ivan Ivanov", "ivan@example.com");

        when(userService.add(any(UserDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));

        verify(userService, times(1)).add(inputDto);
    }

    @Test
    public void getUserByIdTest() throws Exception {
        UserDto userDto = createUserDto(1L, "Ivan Ivanov", "ivan@example.com");

        when(userService.findById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));

        verify(userService, times(1)).findById(1L);
    }

    @Test
    public void getUserByIdNotFoundTest() throws Exception {
        when(userService.findById(99L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserTest() throws Exception {
        UserDto updatedDto = createUserDto(1L, "Petr Petrov", "petr@example.com");

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updatedDto);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Petr Petrov"))
                .andExpect(jsonPath("$.email").value("petr@example.com"));

        verify(userService, times(1)).update(eq(1L), any(UserDto.class));
    }

    @Test
    public void getAllUsersTest() throws Exception {
        UserDto user1 = createUserDto(1L, "Ivan Ivanov", "ivan@example.com");
        UserDto user2 = createUserDto(2L, "Petr Petrov", "petr@example.com");

        when(userService.findAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$[0].email").value("ivan@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Petr Petrov"))
                .andExpect(jsonPath("$[1].email").value("petr@example.com"));

        verify(userService, times(1)).findAll();
    }

    @Test
    public void deleteUserTest() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(1L);
    }

    private UserDto createUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

}
