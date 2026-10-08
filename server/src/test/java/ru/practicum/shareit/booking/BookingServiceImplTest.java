package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class BookingServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private User scammer;
    private Item availableItem;
    private Item unavailableItem;


    @BeforeEach
    void setUp() {
        owner = createUser("Ivan Ivanov", "ivan@example.com");
        booker = createUser("Petr Petrov", "petr@example.com");
        scammer = createUser("Scam Man", "scam@example.com");
        availableItem = createItem(owner, "Дрель", "Описание дрели", true);
        unavailableItem = createItem(owner, "УШМ", "Описание УШМ", false);
    }

    @Test
    public void addBookingTest() {
        BookingDtoInput bookingDtoInput = new BookingDtoInput();
        bookingDtoInput.setItemId(availableItem.getId());
        bookingDtoInput.setStart(LocalDateTime.now().plusDays(1));
        bookingDtoInput.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto booking = bookingService.createBooking(booker.getId(), bookingDtoInput);

        flushAndClear();

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getItem().getId()).isEqualTo(availableItem.getId());
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getStatus()).isEqualTo(Status.WAITING);

    }

    @Test
    public void addBookingUnavailableItemTest() {
        BookingDtoInput bookingDtoInput = new BookingDtoInput();
        bookingDtoInput.setItemId(unavailableItem.getId());
        bookingDtoInput.setStart(LocalDateTime.now().plusDays(1));
        bookingDtoInput.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, ()
                -> bookingService.createBooking(booker.getId(), bookingDtoInput));
    }

    @Test
    public void addBookingByOwnerExceptionTest() {
        BookingDtoInput bookingDtoInput = new BookingDtoInput();
        bookingDtoInput.setItemId(availableItem.getId());
        bookingDtoInput.setStart(LocalDateTime.now().plusDays(1));
        bookingDtoInput.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, ()
                -> bookingService.createBooking(owner.getId(), bookingDtoInput));

    }

    @Test
    public void approveBookingTest() {
        Booking booking = bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build());

        bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertThat(booking.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    public void approveBookingByNotOwnerExceptionTest() {
        Booking booking = bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build());

        flushAndClear();

        assertThrows(AccessDeniedException.class, ()
                -> bookingService.approveBooking(booker.getId(), booking.getId(), true));
    }

    @Test
    public void approveBookingAlreadyApprovedExceptionTest() {
        Booking booking = bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build());

        bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertThrows(ValidationException.class, ()
                -> bookingService.approveBooking(owner.getId(), booking.getId(), true));
    }

    @Test
    public void getBookingByIdTest() {
        Booking booking = bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build());

        BookingDto ownerBookingDto = bookingService.getBookingById(owner.getId(), booking.getId());
        BookingDto bookerBookingDto = bookingService.getBookingById(booker.getId(), booking.getId());

        assertThat(ownerBookingDto.getId()).isEqualTo(bookerBookingDto.getId());
    }

    @Test
    public void getBookingByIdWrongUserExceptionTest() {
        Booking booking = bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build());

        assertThrows(NotFoundException.class, ()
                -> bookingService.getBookingById(scammer.getId(), booking.getId()));
    }

    @Test
    public void getAllBookingsByBookerFiltersTest() {
        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .status(Status.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(Status.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(Status.WAITING)
                .build());

        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(Status.REJECTED)
                .build());

        flushAndClear();

        List<BookingDto> all = bookingService.getBookingsByBooker("ALL", booker.getId());
        List<BookingDto> past = bookingService.getBookingsByBooker("PAST", booker.getId());
        List<BookingDto> current = bookingService.getBookingsByBooker("CURRENT", booker.getId());
        List<BookingDto> future = bookingService.getBookingsByBooker("FUTURE", booker.getId());
        List<BookingDto> waiting = bookingService.getBookingsByBooker("WAITING", booker.getId());
        List<BookingDto> rejected = bookingService.getBookingsByBooker("REJECTED", booker.getId());

        assertThat(all.size()).isEqualTo(4);
        assertThat(past.size()).isEqualTo(1);
        assertThat(current.size()).isEqualTo(1);
        assertThat(future.size()).isEqualTo(2);
        assertThat(waiting.size()).isEqualTo(1);
        assertThat(rejected.size()).isEqualTo(1);
    }

    @Test
    public void getAllBookingsByOwnerFiltersTest() {
        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .status(Status.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(Status.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(Status.WAITING)
                .build());

        bookingRepository.save(Booking.builder()
                .booker(booker)
                .item(availableItem)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(Status.REJECTED)
                .build());

        flushAndClear();

        List<BookingDto> all = bookingService.getBookingsByOwner("ALL", owner.getId());
        List<BookingDto> past = bookingService.getBookingsByOwner("PAST", owner.getId());
        List<BookingDto> current = bookingService.getBookingsByOwner("CURRENT", owner.getId());
        List<BookingDto> future = bookingService.getBookingsByOwner("FUTURE", owner.getId());
        List<BookingDto> waiting = bookingService.getBookingsByOwner("WAITING", owner.getId());
        List<BookingDto> rejected = bookingService.getBookingsByOwner("REJECTED", owner.getId());

        assertThat(all.size()).isEqualTo(4);
        assertThat(past.size()).isEqualTo(1);
        assertThat(current.size()).isEqualTo(1);
        assertThat(future.size()).isEqualTo(2);
        assertThat(waiting.size()).isEqualTo(1);
        assertThat(rejected.size()).isEqualTo(1);
    }

    private User createUser(String name, String email) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }

    private Item createItem(User owner, String name, String description, Boolean available) {
        return itemRepository.save(Item.builder()
                .owner(owner)
                .name(name)
                .description(description)
                .available(available)
                .request(null)
                .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
