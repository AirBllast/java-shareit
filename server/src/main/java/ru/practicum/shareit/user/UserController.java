package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userService.findAll();
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userService.add(userDto);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    @PatchMapping("{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return userService.update(id, userDto);
    }
}