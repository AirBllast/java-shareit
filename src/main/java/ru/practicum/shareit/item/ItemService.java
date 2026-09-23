package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto add(ItemDto itemDto, Long ownerId);

    ItemDto findById(Long itemId, Long userId);

    void delete(long id);

    ItemDto update(ItemDto itemDto, Long ownerId, Long itemId);

    Collection<ItemDto> findAllByOwnerId(Long ownerId);

    Collection<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentDtoInput commentDtoInput);
}