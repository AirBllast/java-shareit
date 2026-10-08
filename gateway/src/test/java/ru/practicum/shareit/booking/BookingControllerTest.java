package ru.practicum.shareit.booking;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private final Long userId = 1L;
    private final Long itemId = 1L;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingClient bookingClient;

    @Test
    public void bookItemWhenValidInputStatus200() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto bookingDto = createBookingDto(itemId, start, end);

        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class))).thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).bookItem(eq(userId), any(BookItemRequestDto.class));
    }

    @Test
    public void bookItemWhenMissingUserHeaderStatus400() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto bookingDto = createBookingDto(itemId, start, end);

        mockMvc.perform(post("/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void bookItemWhenStartInPastStatus400() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto bookingDto = createBookingDto(itemId, start, end);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void bookItemWhenEndBeforeStartStatus400() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookItemRequestDto bookingDto = createBookingDto(itemId, start, end);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void bookItemWhenEndInPastStatus400() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().minusDays(1);
        BookItemRequestDto bookingDto = createBookingDto(itemId, start, end);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void approveBookingWhenValidInputStatus200() throws Exception {
        Long bookingId = 1L;
        Boolean approved = true;

        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId)
                        .param("approved", approved.toString()))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).approveBooking(eq(userId), eq(bookingId), eq(approved));
    }

    @Test
    public void getBookingWhenValidInputStatus200() throws Exception {
        Long bookingId = 1L;

        when(bookingClient.getBooking(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getBooking(eq(userId), eq(bookingId));
    }

    @Test
    public void getBookingWhenInvalidBookingIdStatus400() throws Exception {
        Long bookingId = -1L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_HEADER, userId))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void getBookingsWhenValidInputStatus200() throws Exception {
        String state = "all";
        Integer from = 0;
        Integer size = 10;

        when(bookingClient.getBookings(anyLong(), any(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getBookings(eq(userId), any(), eq(from), eq(size));
    }

    @Test
    public void getBookingsWhenInvalidFromStatus400() throws Exception {
        String state = "all";
        Integer from = -1;
        Integer size = 10;

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void getBookingsWhenUnknownStateStatus400() throws Exception {
        String state = "unknown";
        Integer from = 0;
        Integer size = 10;

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void getBookingsByOwnerWhenValidInputStatus200() throws Exception {
        String state = "all";
        Integer from = 0;
        Integer size = 10;

        when(bookingClient.getOwnerBookings(anyLong(), any(), anyInt(), anyInt())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk());

        verify(bookingClient, times(1)).getOwnerBookings(eq(userId), any(), eq(from), eq(size));
    }

    @Test
    public void getBookingsByOwnerWhenInvalidFromStatus400() throws Exception {
        String state = "all";
        Integer from = -1;
        Integer size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    public void getBookingsByOwnerWhenUnknownStateStatus400() throws Exception {
        String state = "unknown";
        Integer from = 0;
        Integer size = 10;

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }





    private BookItemRequestDto createBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookItemRequestDto dto = new BookItemRequestDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }
}
