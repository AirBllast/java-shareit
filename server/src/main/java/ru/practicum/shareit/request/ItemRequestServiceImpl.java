package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto addRequest(Long userId, ItemRequestDtoInput dto) {
        User user = checkUserId(userId);

        ItemRequest request = itemRequestMapper.mapToItemRequest(dto, user);
        ItemRequest itemRequest = itemRequestRepository.save(request);

        return itemRequestMapper.mapToItemRequestDto(itemRequest, List.of());
    }

    @Override
    public Collection<ItemRequestDto> getUserRequests(Long userId) {
        checkUserId(userId);
        Sort sortByCreatedDesc = Sort.by(Sort.Direction.DESC, "created")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        return toDtos(itemRequestRepository.findAllByRequesterId(userId, sortByCreatedDesc));
    }

    @Override
    public Collection<ItemRequestDto> getAllRequests(Long userId) {
        checkUserId(userId);
        Sort sortByCreatedDesc = Sort.by(Sort.Direction.DESC, "created")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        return toDtos(itemRequestRepository.findAllByRequesterIdNot(userId, sortByCreatedDesc));
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        checkUserId(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id = " + requestId + " не найден"));
        return toDtos(List.of(request)).get(0);
    }

    private User checkUserId(Long userId) {
        if (userId == null) {
            throw new NotFoundException("ID владельца не может быть null");
        }
        return userRepository.findById(userId).orElseThrow(()
                -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private List<ItemRequestDto> toDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        Map<Long, List<ItemShortDto>> itemsByRequest = itemRepository
                .findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(itemMapper::mapToItemShortDto, Collectors.toList())));

        return requests.stream()
                .map(r -> itemRequestMapper.mapToItemRequestDto(
                        r, itemsByRequest.getOrDefault(r.getId(), List.of())))
                .toList();
    }
}
