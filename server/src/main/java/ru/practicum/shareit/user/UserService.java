package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {

    User add(UserDto userDto);

    UserDto findById(Long id);

    Collection<UserDto> findAll();

    void delete(long id);

    User update(Long id, UserDto userDto);
}
