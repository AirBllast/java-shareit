package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long itemId = 1L;

    @Test
    public void createItemTest() throws Exception {
        ItemDto inputDto = createItemDto(null, "Дрель", "Описание", true);
        ItemDto outputDto = createItemDto(itemId, "Дрель", "Описание", true);

        when(itemService.add(any(ItemDto.class), eq(userId))).thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", is("Описание")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService, times(1)).add(inputDto, userId);
    }

    @Test
    public void updateItemTest() throws Exception {
        ItemDto updatedDto = createItemDto(itemId, "Обновленная дрель", "Обновленное описание", true);

        when(itemService.update(any(ItemDto.class), eq(userId), eq(itemId))).thenReturn(updatedDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Обновленная дрель")))
                .andExpect(jsonPath("$.description", is("Обновленное описание")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService, times(1)).update(updatedDto, userId, itemId);
    }

    @Test
    public void getItemByIdTest() throws Exception {
        ItemDto itemDto = createItemDto(itemId, "Дрель", "Описание", true);

        when(itemService.findById(eq(itemId), eq(userId))).thenReturn(itemDto);

        mockMvc.perform(get("/items/{id}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", is("Описание")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService, times(1)).findById(itemId, userId);
    }

    @Test
    public void getItemsTest() throws Exception {
        ItemDto item1 = createItemDto(1L, "Дрель", "Описание", true);
        ItemDto item2 = createItemDto(2L, "УШМ", "Описание", true);

        when(itemService.findAllByOwnerId(eq(userId))).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                .header(USER_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("УШМ"));

        verify(itemService, times(1)).findAllByOwnerId(eq(userId));
    }

    @Test
    public void  searchItemsTest() throws Exception {
        ItemDto itemDto = createItemDto(itemId, "Дрель", "Описание", true);
        List<ItemDto> items = List.of(itemDto);

        when(itemService.search(eq("дрель"))).thenReturn(items);

        mockMvc.perform(get("/items/search").param("text", "дрель")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].description").value("Описание"))
                .andExpect(jsonPath("$[0].available").value(true));

        verify(itemService, times(1)).search(eq("дрель"));
    }

    @Test
    public void addCommentTest() throws Exception {
        CommentDtoInput commentInput = new CommentDtoInput();
        commentInput.setText("Очень полезный отзыв");

        CommentDto commentOutput = new CommentDto();
        commentOutput.setId(1L);
        commentOutput.setText("Очень полезный отзыв");
        commentOutput.setAuthorName("Ivan");
        commentOutput.setCreated(java.time.LocalDateTime.now());

        when(itemService.addComment(eq(userId), eq(itemId), any(CommentDtoInput.class))).thenReturn(commentOutput);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Очень полезный отзыв"))
                .andExpect(jsonPath("$.authorName").value("Ivan"));

        verify(itemService, times(1)).addComment(eq(userId), eq(itemId), any(CommentDtoInput.class));
    }

    private ItemDto createItemDto(Long id, String name, String description, Boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }
}
