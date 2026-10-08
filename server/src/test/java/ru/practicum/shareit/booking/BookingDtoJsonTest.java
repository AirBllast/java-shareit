package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDtoInput;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDtoInput> json;

    @Test
    public void testBookingDtoInputSerialization() throws Exception {
        BookingDtoInput dto = new BookingDtoInput();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2026, 10, 7, 12, 0, 0));
        dto.setEnd(LocalDateTime.of(2026, 10, 8, 12, 0, 0));

        String expectedJson = "{\n" +
                "  \"itemId\": 1,\n" +
                "  \"start\": \"2026-10-07T12:00:00\",\n" +
                "  \"end\": \"2026-10-08T12:00:00\"\n" +
                "}";

        assertThat(json.write(dto)).isEqualToJson(expectedJson);
    }

    @Test
    public void testBookingDtoInputDeserialization() throws Exception {
        String jsonContent = "{\n" +
                "  \"itemId\": 1,\n" +
                "  \"start\": \"2026-10-07T12:00:00\",\n" +
                "  \"end\": \"2026-10-08T12:00:00\"\n" +
                "}";

        BookingDtoInput dto = json.parseObject(jsonContent);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 7, 12, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 10, 8, 12, 0, 0));
    }
}
