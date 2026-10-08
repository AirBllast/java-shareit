package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;
import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getItemById(Long userId, Long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> getItems(Long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        if (text.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> addItem(Long ownerId, ItemDto itemDto) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        itemDto.setId(itemId);
        return patch("/" + itemId, ownerId, itemDto);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDtoInput commentDtoInput) {
        return post("/" + itemId + "/comment", userId, commentDtoInput);
    }
}
