package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    public void testItemDtoSerialization() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Аккумуляторная дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
    }

    @Test
    public void testItemDtoDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Дрель\",\n" +
                "  \"description\": \"Аккумуляторная дрель\",\n" +
                "  \"available\": true\n" +
                "}";

        ItemDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(dto.getAvailable()).isEqualTo(true);
    }
}