package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestService itemRequestService;
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


    @BeforeEach
    void setUp() {
        owner = createUser("Ivan Ivanov", "ivan@example.com");
        booker = createUser("Petr Petrov", "petr@example.com");
    }

    @Test
    void addItemTest() {
        ItemDto inputDto = createItemDto("Дрель", "Мощная дрель", true, null);

        ItemDto createdItem = itemService.add(inputDto, owner.getId());

        assertThat(createdItem.getId()).isNotNull();
        assertThat(createdItem.getName()).isEqualTo("Дрель");
        assertThat(createdItem.getDescription()).isEqualTo("Мощная дрель");
        assertThat(createdItem.getAvailable()).isTrue();

    }

    @Test
    void addItemWithRequestTest() {
        ItemRequestDto request = itemRequestService.addRequest(
                booker.getId(), createRequestDto("Нужна дрель"));

        ItemDto inputDto = createItemDto("Дрель", "Мощная дрель", true, request.getId());
        ItemDto createdItem = itemService.add(inputDto, owner.getId());

        assertThat(createdItem.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void addItemUserNotFoundTest() {
        ItemDto inputDto = createItemDto("Дрель", "Мощная дрель", true, null);

        assertThrows(NotFoundException.class, () -> itemService.add(inputDto, 99L));
    }

    @Test
    void searchItemTest() {
        itemService.add(createItemDto("Дрель Доступная", "Инструмент",
                true, null), owner.getId());
        itemService.add(createItemDto("Молоток", "Супер ДРЕЛЬ",
                true, null), owner.getId());
        itemService.add(createItemDto("Дрель Недоступная", "Инструмент",
                false, null), owner.getId());

        flushAndClear();

        Collection<ItemDto> result = itemService.search("дРеЛь");

        assertThat(result)
                .hasSize(2) //
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель Доступная", "Молоток");
    }

    @Test
    void searchItemEmptyQueryTest() {
        itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        Collection<ItemDto> result = itemService.search("");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdItemTest() {
        ItemDto inputDto = createItemDto("Дрель", "Мощная дрель", true, null);

        ItemDto createdItem = itemService.add(inputDto, owner.getId());

        flushAndClear();

        ItemDto foundItem = itemService.findById(createdItem.getId(), owner.getId());

        assertThat(foundItem.getId()).isEqualTo(createdItem.getId());
        assertThat(foundItem.getName()).isEqualTo("Дрель");
    }

    @Test
    void findByIdItemNotFoundTest() {
        assertThrows(NotFoundException.class, () -> itemService.findById(99L, owner.getId()));
    }

    @Test
    void findAllByOwnerIdTest() {
        itemService.add(createItemDto("Дрель", "Описание 1",
                true, null), owner.getId());
        itemService.add(createItemDto("Отвертка", "Описание 2",
                true, null), owner.getId());

        flushAndClear();

        Collection<ItemDto> ownerItems = itemService.findAllByOwnerId(owner.getId());

        assertThat(ownerItems)
                .hasSize(2)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Отвертка");
    }

    @Test
    void updateItemTest() {
        ItemDto createdItem = itemService.add(createItemDto("Дрель", "Старое описание",
                true, null), owner.getId());

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Перфоратор");
        updateDto.setDescription("Новое описание");

        ItemDto updatedItem = itemService.update(updateDto, owner.getId(), createdItem.getId());

        assertThat(updatedItem.getName()).isEqualTo("Перфоратор");
        assertThat(updatedItem.getDescription()).isEqualTo("Новое описание");
        assertThat(updatedItem.getAvailable()).isTrue();
    }

    @Test
    void updateItemNotOwnerTest() {
        ItemDto createdItem = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Новое имя");

        assertThrows(NotFoundException.class,
                () -> itemService.update(updateDto, booker.getId(), createdItem.getId()));
    }

    @Test
    void deleteItemTest() {
        ItemDto createdItem = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        itemService.delete(createdItem.getId());
        flushAndClear();

        assertThrows(NotFoundException.class, () -> itemService.findById(createdItem.getId(), owner.getId()));
    }

    @Test
    void addCommentTest() {
        ItemDto item = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());
        Item itemEntity = itemRepository.findById(item.getId()).orElseThrow();
        User bookerEntity = userRepository.findById(booker.getId()).orElseThrow();

        Booking booking = new Booking();
        booking.setItem(itemEntity);
        booking.setBooker(bookerEntity);
        booking.setStart(LocalDateTime.now().minusDays(5));
        booking.setEnd(LocalDateTime.now().minusDays(4));
        booking.setStatus(Status.APPROVED);

        bookingRepository.save(booking);

        flushAndClear();

        CommentDtoInput commentInput = new CommentDtoInput();
        commentInput.setText("Очень полезный отзыв");

        CommentDto commentDto = itemService.addComment(booker.getId(), item.getId(), commentInput);

        assertThat(commentDto.getId()).isNotNull();
        assertThat(commentDto.getText()).isEqualTo("Очень полезный отзыв");
        assertThat(commentDto.getAuthorName()).isEqualTo("Petr Petrov");
    }

    @Test
    void addCommentWithoutBookingTest() {
        ItemDto item = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        CommentDtoInput commentInput = new CommentDtoInput();
        commentInput.setText("Очень полезный отзыв");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), commentInput));
    }


    @Test
    void getByIdByOwnerWithBookingsTest() {
        ItemDto item = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        BookingDtoInput lastBooking = new BookingDtoInput();
        lastBooking.setItemId(item.getId());
        lastBooking.setStart(LocalDateTime.now().minusDays(5));
        lastBooking.setEnd(LocalDateTime.now().minusDays(4));
        BookingDto booking1 = bookingService.createBooking(booker.getId(), lastBooking);
        bookingService.approveBooking(owner.getId(), booking1.getId(), true);

        BookingDtoInput nextBooking = new BookingDtoInput();
        nextBooking.setItemId(item.getId());
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));
        BookingDto booking2 = bookingService.createBooking(booker.getId(), nextBooking);
        bookingService.approveBooking(owner.getId(), booking2.getId(), true);

        flushAndClear();

        ItemDto result = itemService.findById(item.getId(), owner.getId());

        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getLastBooking().getId()).isEqualTo(booking1.getId());
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(booking2.getId());
    }

    @Test
    void getByIdByNonOwnerWithoutBookingsTest() {
        ItemDto item = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        BookingDtoInput lastBooking = new BookingDtoInput();
        lastBooking.setItemId(item.getId());
        lastBooking.setStart(LocalDateTime.now().minusDays(5));
        lastBooking.setEnd(LocalDateTime.now().minusDays(4));
        var b1 = bookingService.createBooking(booker.getId(), lastBooking);
        bookingService.approveBooking(owner.getId(), b1.getId(), true);

        flushAndClear();

        ItemDto result = itemService.findById(item.getId(), booker.getId());

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void updateItemPartialTest() {
        ItemDto created = itemService.add(createItemDto("Дрель", "Старое описание",
                true, null), owner.getId());

        ItemDto patchDto = new ItemDto();
        patchDto.setName("Новая Дрель");

        ItemDto updated = itemService.update(patchDto, owner.getId(), created.getId());

        assertThat(updated.getName()).isEqualTo("Новая Дрель");
        assertThat(updated.getDescription()).isEqualTo("Старое описание"); // Не изменилось
        assertThat(updated.getAvailable()).isTrue(); // Не изменилось
    }

    @Test
    void addCommentFutureBookingTest() {
        ItemDto item = itemService.add(createItemDto("Дрель", "Описание",
                true, null), owner.getId());

        BookingDtoInput bookingInput = new BookingDtoInput();
        bookingInput.setItemId(item.getId());
        bookingInput.setStart(LocalDateTime.now().plusDays(1));
        bookingInput.setEnd(LocalDateTime.now().plusDays(2));

        var createdBooking = bookingService.createBooking(booker.getId(), bookingInput);
        bookingService.approveBooking(owner.getId(), createdBooking.getId(), true);

        flushAndClear();

        CommentDtoInput commentInput = new CommentDtoInput();
        commentInput.setText("Очень полезный отзыв");

        assertThrows(ValidationException.class,
                () -> itemService.addComment(booker.getId(), item.getId(), commentInput));
    }

    private ItemRequestDtoInput createRequestDto(String description) {
        ItemRequestDtoInput dto = new ItemRequestDtoInput();
        dto.setDescription(description);
        return dto;
    }

    private ItemDto createItemDto(String name, String description, Boolean available, Long requestId) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        return dto;
    }


    private User createUser(String name, String email) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
