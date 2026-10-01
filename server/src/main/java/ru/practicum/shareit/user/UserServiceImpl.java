package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
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

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public User add(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicatedDataException("Пользователь с email = " + userDto.getEmail() + " уже существует");
        }
        User user = userMapper.mapToUser(userDto);
        return userRepository.save(user);
    }

    @Override
    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    @Override
    public Collection<UserDto> findAll() {
        return userMapper.mapToUserDto(userRepository.findAll());
    }

    @Override
    public void delete(long id) {
        userRepository.deleteById(id);
    }
}
