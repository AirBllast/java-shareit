package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long itemId = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemClient itemClient;

    @Test
    public void getItemByIdWhenValidHeadersAndParamsStatus200() throws Exception {

        when(itemClient.getItemById(userId, itemId))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).getItemById(userId, itemId);
    }

    @Test
    public void getItemByIdWhenMissingUserHeaderStatus400() throws Exception {

        mockMvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void getItemByIdWhenInvalidUserHeaderStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, invalidUserId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void getItemsWhenValidHeadersStatus200() throws Exception {
        ItemDto item1 = createItemDto(1L, "Дрель", "Описание Дрели", true);
        ItemDto item2 = createItemDto(2L, "УШМ", "Описание УШМ", false);
        List<ItemDto> items = List.of(item1, item2);

        when(itemClient.getItems(userId))
                .thenReturn(ResponseEntity.ok(items));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("УШМ"));

        verify(itemClient, times(1)).getItems(userId);
    }

    @Test
    public void getItemsWhenMissingUserHeaderStatus400() throws Exception {

        mockMvc.perform(get("/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void getItemsWhenInvalidUserHeaderStatus400() throws Exception {
        Long invalidUserId = -1L;

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, invalidUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void createItemDtoWhenValidStatus200() throws Exception {
        ItemDto dto = createItemDto(itemId, "Дрель", "Описание Дрели", true);

        when(itemClient.addItem(eq(userId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Описание Дрели"))
                .andExpect(jsonPath("$.available").value(true));

        verify(itemClient, times(1)).addItem(eq(userId), any(ItemDto.class));
    }

    @Test
    public void createItemWhenMissingUserHeaderStatus400() throws Exception {
        ItemDto dto = createItemDto(itemId, "Дрель", "Описание Дрели", true);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void createItemWhenBlankNameStatus400() throws Exception {
        ItemDto dto = createItemDto(itemId, "", "Описание Дрели", true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void createItemWhenBlankDescriptionStatus400() throws Exception {
        ItemDto dto = createItemDto(itemId, "Дрель", "", true);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void createItemWhenNullAvailableStatus400() throws Exception {
        ItemDto dto = createItemDto(itemId, "Дрель", "Описание Дрели", null);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void updateItemDtoWhenValidStatus200() throws Exception {
        ItemDto dto = createItemDto(itemId, "Новая Дрель", "Новое Описание Дрели", false);

        when(itemClient.updateItem(eq(userId), eq(itemId), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(dto));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Новая Дрель"))
                .andExpect(jsonPath("$.description").value("Новое Описание Дрели"))
                .andExpect(jsonPath("$.available").value(false));

        verify(itemClient, times(1)).updateItem(eq(userId), eq(itemId), any(ItemDto.class));
    }

    @Test
    public void updateItemWhenInvalidUserIdStatus400() throws Exception {
        ItemDto dto = createItemDto(itemId, "Новая Дрель", "Новое Описание Дрели", false);
        Long invalidUserId = -1L;

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void updateItemWhenInvalidItemIdStatus400() throws Exception {
        ItemDto dto = createItemDto(itemId, "Новая Дрель", "Новое Описание Дрели", false);
        Long invalidItemId = 0L;

        mockMvc.perform(patch("/items/{itemId}", invalidItemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    public void searchItemWhenValidStatus200() throws Exception {
        String searchText = "дрель";

        when(itemClient.searchItems(searchText))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).searchItems(searchText);
    }

    @Test
    public void searchItemWhenBlankTextStatus200() throws Exception {
        String searchText = "   ";

        when(itemClient.searchItems(searchText))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemClient, times(1)).searchItems(searchText);
    }

    @Test
    public void addCommentWhenValidStatus200() throws Exception {
        String commentText = "Отличный инструмент!";
        CommentDtoInput commentDtoInput = new CommentDtoInput();
        commentDtoInput.setText(commentText);

        when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDtoInput.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDtoInput)))
                .andExpect(status().isOk());

        verify(itemClient, times(1)).addComment(eq(userId), eq(itemId), any(CommentDtoInput.class));
    }

    @Test
    public void addCommentWhenBlankTextStatus400() throws Exception {
        String commentText = "   ";
        CommentDtoInput commentDtoInput = new CommentDtoInput();
        commentDtoInput.setText(commentText);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDtoInput)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    private ItemDto createItemDto(Long id, String name, String description, Boolean available) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

}
