package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto addRequest(Long userId, ItemRequestDtoInput dto);

    Collection<ItemRequestDto> getUserRequests(Long userId);

    Collection<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
