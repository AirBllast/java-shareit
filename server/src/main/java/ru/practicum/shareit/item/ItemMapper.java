package ru.practicum.shareit.item;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Component
@NoArgsConstructor
public class ItemMapper {

    public ItemDto mapToItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                null,
                null,
                null
        );
    }

    public Item mapToItem(ItemDto itemDto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setOwner(owner);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequest(request);
        return item;
    }

    public List<ItemDto> mapToItemDto(Iterable<Item> items) {
        if (items == null) {
            return List.of();
        }
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            result.add(mapToItemDto(item));
        }

        return result;
    }

    public ItemShortDto mapToItemShortDto(Item item) {
        return new ItemShortDto(item.getId(), item.getName(), item.getOwner().getId());
    }

}
