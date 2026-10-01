package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long userId, BookingDtoInput bookingDtoInput);

    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByBooker(String state, Long userId);

    List<BookingDto> getBookingsByOwner(String state, Long userId);
}
