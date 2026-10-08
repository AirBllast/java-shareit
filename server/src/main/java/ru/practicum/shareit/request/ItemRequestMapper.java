package ru.practicum.shareit.request;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
@NoArgsConstructor
public class ItemRequestMapper {

    public ItemRequest mapToItemRequest(ItemRequestDtoInput itemRequestDtoInput, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDtoInput.getDescription());
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(requester);
        return itemRequest;
    }

    public ItemRequestDto mapToItemRequestDto(ItemRequest request, List<ItemShortDto> items) {
        return new ItemRequestDto(request.getId(), request.getDescription(),
                request.getCreated(), items);
    }


}
