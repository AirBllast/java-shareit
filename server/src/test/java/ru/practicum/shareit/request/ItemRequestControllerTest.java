package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long requestId = 1L;

    @Test
    public void createItemRequestTest() throws Exception {
        ItemRequestDtoInput inputDto = createItemRequestDtoInput("Описание запроса");
        ItemRequestDto outputDto = createItemRequestDto(1L, "Описание запроса");

        when(itemRequestService.addRequest(eq(userId), any(ItemRequestDtoInput.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Описание запроса"));

        verify(itemRequestService, times(1)).addRequest(userId, inputDto);
    }

    @Test
    public void getUserRequestsTest() throws Exception {
        ItemRequestDto outputDto = createItemRequestDto(1L, "Описание запроса");
        List<ItemRequestDto> requests = List.of(outputDto);

        when(itemRequestService.getUserRequests(userId))
                .thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Описание запроса"));

        verify(itemRequestService, times(1)).getUserRequests(userId);
    }

    @Test
    public void getAllRequestsTest() throws Exception {
        ItemRequestDto outputDto = createItemRequestDto(1L, "Описание запроса");
        List<ItemRequestDto> requests = List.of(outputDto);

        when(itemRequestService.getAllRequests(userId))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Описание запроса"));

        verify(itemRequestService, times(1)).getAllRequests(userId);
    }

    @Test
    public void getRequestByIdTest() throws Exception {
        ItemRequestDto outputDto = createItemRequestDto(1L, "Описание запроса");

        when(itemRequestService.getRequestById(userId, requestId))
                .thenReturn(outputDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Описание запроса"));

        verify(itemRequestService, times(1)).getRequestById(userId, requestId);
    }

    private ItemRequestDto createItemRequestDto(Long id, String description) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(id);
        itemRequestDto.setDescription(description);
        itemRequestDto.setCreated(LocalDateTime.now().plusDays(1));
        return itemRequestDto;
    }

    private ItemRequestDtoInput createItemRequestDtoInput(String description) {
        ItemRequestDtoInput itemRequestDtoInput = new ItemRequestDtoInput();
        itemRequestDtoInput.setDescription(description);
        return itemRequestDtoInput;
    }

}
