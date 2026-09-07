package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Item add(Item item);

   Optional<Item> findById(Long id);

    void delete(long id);

    Collection<Item> findAllByOwnerId(Long ownerId);

    Collection<Item> search(String text);

    Item update(Item item);
}
