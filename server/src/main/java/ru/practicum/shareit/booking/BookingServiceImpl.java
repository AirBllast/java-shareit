package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingDtoInput bookingDtoInput) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(bookingDtoInput.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою же вещь");
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь не доступна для бронирования");
        }

        if (!bookingDtoInput.getEnd().isAfter(bookingDtoInput.getStart())) {
            throw new ValidationException("Окончание брони не может быть раньше начала");
        }

        Booking booking = bookingMapper.mapToBooking(bookingDtoInput, item, booker);
        bookingRepository.save(booking);

        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Только владелец может подтвердить бронирование");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Заявка уже обработана");
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только арендатор или владелец могут посмотреть бронирование");
        }

        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(String state, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> bookings = switch (stateCheck(state)) {
            case CURRENT -> bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, sortByStartDesc);
            case PAST -> bookingRepository.findByBooker_IdAndEndIsBefore(userId, now, sortByStartDesc);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartIsAfter(userId, now, sortByStartDesc);
            case WAITING -> bookingRepository.findByBooker_IdAndStatus(userId, Status.WAITING, sortByStartDesc);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatus(userId, Status.REJECTED, sortByStartDesc);
            case ALL -> bookingRepository.findByBooker_Id(userId, sortByStartDesc);
        };
        return bookingMapper.mapToBookingDtoList(bookings);
    }

    @Override
    public List<BookingDto> getBookingsByOwner(String state, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> bookings = switch (stateCheck(state)) {
            case CURRENT -> bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(userId, now, now, sortByStartDesc);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, sortByStartDesc);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now, sortByStartDesc);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.WAITING, sortByStartDesc);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatus(userId, Status.REJECTED, sortByStartDesc);
            case ALL -> bookingRepository.findByItem_Owner_Id(userId, sortByStartDesc);
        };
        return bookingMapper.mapToBookingDtoList(bookings);
    }

    private State stateCheck(String state) {
        if (state == null) {
            return State.ALL;
        }
        try {
            return State.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестный параметр state " + state);
        }
    }
}
