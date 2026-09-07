package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    User add(User user);

    User update(User user);

    Optional<User> findById(Long id);

    Collection<User> findAll();

    void delete(long id);

    boolean existsByEmail(String email);
}
