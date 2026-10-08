package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                              @PathVariable @Positive Long id) {
        return itemClient.getItemById(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") @Positive Long ownerId) {
        return itemClient.getItems(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        return itemClient.searchItems(text);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
                        @Valid @RequestBody ItemDto itemDto) {
        return itemClient.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(value = "X-Sharer-User-Id") @Positive  Long ownerId,
                                           @PathVariable @Positive Long itemId, @Valid @RequestBody ItemDto itemDto) {
        itemDto.setId(itemId);
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                            @PathVariable @Positive Long itemId,
                                            @Valid @RequestBody CommentDtoInput commentDtoInput) {
        return itemClient.addComment(userId, itemId, commentDtoInput);
    }
}
