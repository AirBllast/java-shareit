package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto add(ItemDto itemDto, Long ownerId);

    ItemDto findById(Long id);

    void delete(long id);

    ItemDto update(ItemDto itemDto, Long ownerId, Long itemId);

    Collection<ItemDto> findAllByOwnerId(Long ownerId);

    Collection<ItemDto> search(String text);
}
