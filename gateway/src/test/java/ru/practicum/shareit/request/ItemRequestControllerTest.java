package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long requestId = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    public void addRequestWhenValidInputStatus200() throws Exception {
        ItemRequestDtoInput itemRequestDtoInput = createItemRequestDtoInput();

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDtoInput)))
                .andExpect(status().isOk());

        verify(itemRequestClient, times(1)).addRequest(eq(userId), any(ItemRequestDtoInput.class));
    }

    @Test
    public void addRequestWhenInvalidUserIdStatus400() throws Exception {
        ItemRequestDtoInput itemRequestDtoInput = createItemRequestDtoInput();
        Long invalidUserId = -1L;

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, invalidUserId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDtoInput)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    public void addRequestWhenBlankDescriptionStatus400() throws Exception {
        ItemRequestDtoInput itemRequestDtoInput = new ItemRequestDtoInput();
        itemRequestDtoInput.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemRequestDtoInput)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    public void getUserRequestsWhenValidUserIdStatus200() throws Exception {
        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk());

        verify(itemRequestClient, times(1)).getUserRequests(userId);
    }

    @Test
    public void getUserRequestsWhenInvalidUserIdStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, invalidUserId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    public void getAllRequestsWhenValidUserIdStatus200() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, userId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemRequestClient, times(1)).getAllRequests(userId, 0, 10);
    }

    @Test
    public void getAllRequestsWhenInvalidUserIdStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, invalidUserId)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    public void getRequestByIdWhenValidUserIdAndRequestIdStatus200() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk());

        verify(itemRequestClient, times(1)).getRequestById(userId, requestId);
    }

    @Test
    public void getRequestByIdWhenInvalidUserIdStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_HEADER, invalidUserId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    public void getRequestByIdWhenInvalidRequestIdStatus400() throws Exception {
        Long invalidRequestId = -1L;

        mockMvc.perform(get("/requests/{requestId}", invalidRequestId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    private ItemRequestDtoInput createItemRequestDtoInput() {
        ItemRequestDtoInput itemRequestDtoInput = new ItemRequestDtoInput();
        itemRequestDtoInput.setDescription("Какое то описание");
        return itemRequestDtoInput;
    }
}
