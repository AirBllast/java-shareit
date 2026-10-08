package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserClient userClient;

    private final Long userId = 1L;

    @Test
    public void createUserWhenValidStatus200() throws Exception {
        UserDto dto = createUserDto(userId, "Ivan Ivanov", "ivan@example.com");

        ResponseEntity<Object> response = ResponseEntity.ok(dto);

        when(userClient.createUser(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).createUser(any(UserDto.class));
    }

    @Test
    public void createUserWhenBlankNameStatus400() throws Exception {
        UserDto dto = createUserDto(userId, "  ", "updated@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    public void createUserWhenInvalidEmailStatus400() throws Exception {
        UserDto dto = createUserDto(userId, "Ivan Ivanov", "invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    public void updateUserWhenValidStatus200() throws Exception {
        UserDto dto = createUserDto(userId, "Ivan Ivanov", "ivan@example.com");

        ResponseEntity<Object> response = ResponseEntity.ok(dto);

        when(userClient.updateUser(eq(userId), any(UserDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userClient, times(1)).updateUser(eq(userId), any(UserDto.class));
    }

    @Test
    public void getUserByIdWhenValidStatus200() throws Exception {
        UserDto dto = createUserDto(userId, "Ivan Ivanov", "ivan@example.com");

        ResponseEntity<Object> response = ResponseEntity.ok(dto);

        when(userClient.getUserById(eq(userId))).thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient, times(1)).getUserById(eq(userId));
    }

    @Test
    public void getUserByIdWhenNegativeIdStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(get("/users/{id}", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    public void getUserByIdWhenIdIsZeroStatus400() throws Exception {
        Long invalidUserId = 0L;

        mockMvc.perform(get("/users/{id}", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    public void deleteUserWhenValidStatus200() throws Exception {
        UserDto dto = createUserDto(userId, "Ivan Ivanov", "ivan@example.com");

        ResponseEntity<Object> response = ResponseEntity.ok(dto);

        when(userClient.deleteUser(eq(userId))).thenReturn(response);

        mockMvc.perform(delete("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(eq(userId));
    }

    @Test
    public void deleteUserWhenNegativeIdStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(delete("/users/{id}", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    public void deleteUserWhenIdIsZeroStatus400() throws Exception {
        Long invalidUserId = 0L;

        mockMvc.perform(delete("/users/{id}", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    public void getUsersListStatus200() throws Exception {
        UserDto user1 = createUserDto(1L, "Ivan Ivanov", "ivan@example.com");
        UserDto user2 = createUserDto(2L, "Petr Petrov", "petr@example.com");

        List<UserDto> users = List.of(user1, user2);

        when(userClient.getUsers())
                .thenReturn(ResponseEntity.ok(users));

        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Petr Petrov"));

        verify(userClient, times(1)).getUsers();
    }

    private UserDto createUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}
