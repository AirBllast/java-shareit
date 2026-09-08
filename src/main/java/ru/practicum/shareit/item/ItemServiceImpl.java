package ru.practicum.shareit.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.Collections;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto add(ItemDto itemDto, Long ownerId) {
        if (ownerId == null) {
            throw new NotFoundException("ID владельца не может быть null");
        }
        userService.findById(ownerId);

        Item item = ItemMapper.mapToItem(itemDto, ownerId);
        Item newItem = itemRepository.add(item);
        return ItemMapper.mapToItemDto(newItem);
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.mapToItemDto(itemRepository.search(text));
    }

    @Override
    public ItemDto findById(Long id) {
        return itemRepository.findById(id)
                .map(ItemMapper::mapToItemDto)
                .orElseThrow(() -> new NotFoundException("Предмет с id = " + id + " не найден"));
    }

    @Override
    public Collection<ItemDto> findAllByOwnerId(Long ownerId) {
        return ItemMapper.mapToItemDto(itemRepository.findAllByOwnerId(ownerId));
    }

    @Override
    public void delete(long id) {
        itemRepository.delete(id);
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (ownerId == null) {
            throw new NotFoundException("ID владельца не может быть null");
        }
        userService.findById(ownerId);

        Item newItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id = " + itemId + " не найден"));

        if (!newItem.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не является владельцем предмета с id = " + itemId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            newItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            newItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            newItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(newItem);
        return ItemMapper.mapToItemDto(updatedItem);
    }
}
