package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));

        String newEmail = userDto.getEmail();
        if (newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(existingUser.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new DuplicatedDataException("Пользователь с email = " + newEmail + " уже существует");
            }
            existingUser.setEmail(newEmail);
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        return userRepository.update(existingUser);
    }

    @Override
    public User add(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicatedDataException("Пользователь с email = " + userDto.getEmail() + " уже существует");
        }
        User user = UserMapper.mapToUser(userDto);
        return userRepository.add(user);
    }

    @Override
    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    @Override
    public Collection<UserDto> findAll() {
        return UserMapper.mapToUserDto(userRepository.findAll());
    }

    @Override
    public void delete(long id) {
        userRepository.delete(id);
    }
}
