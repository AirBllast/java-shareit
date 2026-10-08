package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;


@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/users")
@RestController
public class UserController {
    private final UserClient userClient;

    @GetMapping("{id}")
    public ResponseEntity<Object> getUserById(@PathVariable @Positive Long id) {
        return userClient.getUserById(id);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return userClient.getUsers();
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        return userClient.createUser(userDto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable @Positive Long id) {
       return userClient.deleteUser(id);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> updateUser(@PathVariable @Positive Long id, @Valid @RequestBody UserDto userDto) {
        return userClient.updateUser(id, userDto);
    }
}
