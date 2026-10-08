package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    public void testRequestDtoSerialization() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1L);
        itemRequestDto.setDescription("Требуется дрель");
        itemRequestDto.setCreated(LocalDateTime.of(2026, 10, 7, 12, 0, 0));

        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Требуется дрель");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-10-07T12:00:00");
    }

    @Test
    public void testRequestDtoDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 1,\n" +
                "  \"description\": \"Требуется дрель\",\n" +
                "  \"created\": \"2026-10-07T12:00:00\"\n" +
                "}";

        ItemRequestDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Требуется дрель");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 10, 7, 12, 0, 0));
    }
}


