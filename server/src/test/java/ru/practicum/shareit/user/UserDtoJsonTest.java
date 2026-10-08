package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    public void testUserDtoSerialization() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Ivan Ivanov");
        userDto.setEmail("ivan@example.com");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Ivan Ivanov");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@example.com");
    }

    @Test
    public void testUserDtoDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Ivan Ivanov\",\n" +
                "  \"email\": \"ivan@example.com\"\n" +
                "}";

        UserDto dto = json.parseObject(jsonContent);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Ivan Ivanov");
        assertThat(dto.getEmail()).isEqualTo("ivan@example.com");
    }
}
