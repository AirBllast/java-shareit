package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDto mapToItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest()
        );
    }

    public static Item mapToItem(ItemDto itemDto, Long ownerId) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setOwnerId(ownerId);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequest(itemDto.getRequest());
        return item;
    }

    public static List<ItemDto> mapToItemDto(Iterable<Item> items) {
        if (items == null) {
            return List.of();
        }
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            result.add(mapToItemDto(item));
        }

        return result;
    }

}
