package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final Long itemId = 1L;

    @Test
    public void createBookingTest() throws Exception {
        BookingDtoInput inputDto = createBookingDtoInput(
                itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
        BookingDto outputDto = createBookingDto(bookingId, Status.WAITING);

        when(bookingService.createBooking(eq(userId), any(BookingDtoInput.class)))
                .thenReturn(outputDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService, times(1)).createBooking(userId, inputDto);
    }

    @Test
    public void approveBookingTest() throws Exception {
        BookingDto outputDto = createBookingDto(bookingId, Status.APPROVED);

        when(bookingService.approveBooking(eq(userId), eq(bookingId), eq(true)))
                .thenReturn(outputDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService, times(1)).approveBooking(userId, bookingId, true);
    }

    @Test
    public void getBookingByIdTest() throws Exception {
        BookingDto outputDto = createBookingDto(bookingId, Status.WAITING);

        when(bookingService.getBookingById(eq(userId), eq(bookingId)))
                .thenReturn(outputDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService, times(1)).getBookingById(userId, bookingId);
    }

    @Test
    public void getBookingsByBookerTest() throws Exception {
        BookingDto outputDto = createBookingDto(bookingId, Status.WAITING);
        List<BookingDto> bookings = List.of(outputDto);

        when(bookingService.getBookingsByBooker(eq("ALL"), eq(userId)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService, times(1)).getBookingsByBooker("ALL", userId);
    }

    @Test
    public void getBookingsByOwnerTest() throws Exception {
        BookingDto outputDto = createBookingDto(bookingId, Status.WAITING);
        List<BookingDto> bookings = List.of(outputDto);

        when(bookingService.getBookingsByOwner(eq("ALL"), eq(userId)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));

        verify(bookingService, times(1)).getBookingsByOwner("ALL", userId);
    }


    private BookingDto createBookingDto(Long id, Status status) {
        BookingDto dto = new BookingDto();
        dto.setId(id);
        dto.setItem(createItemDto(itemId, "Дрель", "Описание", true));
        dto.setBooker(createUserDto(userId, "Petr Petrov", "petr@example.com"));
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setStatus(status);

        return dto;
    }

    private BookingDtoInput createBookingDtoInput(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDtoInput dto = new BookingDtoInput();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    private ItemDto createItemDto(Long id, String name, String description, Boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }

    private UserDto createUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}
