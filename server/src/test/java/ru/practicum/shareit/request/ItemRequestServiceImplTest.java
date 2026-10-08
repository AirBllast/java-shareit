package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User requester;

    @BeforeEach
    void setUp() {
        owner = createUser("Ivan Ivanov", "ivan@example.com");
        requester = createUser("Petr Petrov", "petr@example.com");
    }

    @Test
    void addItemRequestTest() {
        ItemRequestDto result = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужна дрель"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void addItemRequestUserNotFoundTest() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.addRequest(999L, createRequestDto("Нужна дрель")));
    }

    @Test
    void getUserRequestsTest() {
        ItemRequestDto first = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужна дрель"));
        ItemRequestDto second = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужен перфоратор"));

        ItemDto item = createItemDto(owner.getId(), first.getId(), "Дрель");

        flushAndClear();

        List<ItemRequestDto> result = new ArrayList<>(itemRequestService.getUserRequests(requester.getId()));

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequestDto::getId)
                .containsExactly(second.getId(), first.getId());

        assertThat(result.get(0).getItems()).isEmpty();

        assertThat(result.get(1).getItems())
                .hasSize(1)
                .first()
                .satisfies(answer -> {
                    assertThat(answer.getItemId()).isEqualTo(item.getId());
                    assertThat(answer.getName()).isEqualTo("Дрель");
                    assertThat(answer.getOwnerId()).isEqualTo(owner.getId());
                });
    }

    @Test
    void getAllRequestsTest() {
        ItemRequestDto first = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужна дрель"));
        ItemRequestDto second = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужен перфоратор"));

        ItemRequestDto ownRequest = itemRequestService.addRequest(
                owner.getId(), createRequestDto("Нужна лестница"));

        ItemDto item = createItemDto(owner.getId(), first.getId(), "Дрель");

        flushAndClear();

        List<ItemRequestDto> result = new ArrayList<>(itemRequestService.getAllRequests(owner.getId()));

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequestDto::getId)
                .containsExactly(second.getId(), first.getId())
                .doesNotContain(ownRequest.getId());

        assertThat(result.get(0).getItems()).isEmpty();

        assertThat(result.get(1).getItems())
                .hasSize(1)
                .first()
                .satisfies(answer -> {
                    assertThat(answer.getItemId()).isEqualTo(item.getId());
                    assertThat(answer.getName()).isEqualTo("Дрель");
                    assertThat(answer.getOwnerId()).isEqualTo(owner.getId());
                });
    }

    @Test
    void getRequestByIdTest() {
        ItemRequestDto first = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужна дрель"));
        ItemDto item = createItemDto(owner.getId(), first.getId(), "Дрель");

        flushAndClear();

        ItemRequestDto requestDto = itemRequestService.getRequestById(owner.getId(), first.getId());

        assertThat(requestDto.getId()).isEqualTo(first.getId());
        assertThat(requestDto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(requestDto.getCreated()).isNotNull();

        assertThat(requestDto.getItems())
                .hasSize(1)
                .first()
                .satisfies(answer -> {
                    assertThat(answer.getItemId()).isEqualTo(item.getId());
                    assertThat(answer.getName()).isEqualTo("Дрель");
                    assertThat(answer.getOwnerId()).isEqualTo(owner.getId());
                });
    }

    @Test
    void getRequestByIdRequestNotFoundTest() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requester.getId(), 999L));
    }

    @Test
    void getRequestByIdUserNotFoundTest() {
        ItemRequestDto first = itemRequestService.addRequest(
                requester.getId(), createRequestDto("Нужна дрель"));

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(999L, first.getId()));
    }


    private User createUser(String name, String email) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }

    private ItemRequestDtoInput createRequestDto(String description) {
        ItemRequestDtoInput dto = new ItemRequestDtoInput();
        dto.setDescription(description);
        return dto;
    }

    private ItemDto createItemDto(Long ownerId, Long requestId, String name) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription("Описание: " + name);
        dto.setAvailable(true);
        dto.setRequestId(requestId);
        return itemService.add(dto, ownerId);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}