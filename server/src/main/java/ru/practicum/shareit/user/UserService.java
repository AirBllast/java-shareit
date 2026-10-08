package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto add(UserDto userDto);

    UserDto findById(Long id);

    Collection<UserDto> findAll();

    void delete(long id);

    UserDto update(Long id, UserDto userDto);
}
