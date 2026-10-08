package ru.practicum.shareit.user;


import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void addUserTest() {
        UserDto userDto = new UserDto();
        userDto.setEmail("ivan@example.com");
        userDto.setName("Ivan Ivanov");

        UserDto user = userService.add(userDto);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("Ivan Ivanov");
        assertThat(user.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    public void addUserWithSameEmailTest() {
        UserDto userDto = new UserDto();
        userDto.setEmail("ivan@example.com");
        userDto.setName("Ivan Ivanov");

        userService.add(userDto);

        UserDto userWithSameEmail = new UserDto();
        userWithSameEmail.setName("Ivan Frolov");
        userWithSameEmail.setEmail("ivan@example.com");

        assertThrows(DuplicatedDataException.class, () -> userService.add(userWithSameEmail));
    }

    @Test
    public void updateUserTest() {
        User user = createUser("Ivan Ivanov", "ivan@example.com");

        UserDto updatedDataUser = new UserDto();
        updatedDataUser.setName("Ivan Frolov");
        updatedDataUser.setEmail("frolov@example.com");

        UserDto updatedUser = userService.update(user.getId(), updatedDataUser);

        assertThat(user.getId()).isEqualTo(updatedUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("frolov@example.com");
        assertThat(updatedUser.getName()).isEqualTo("Ivan Frolov");
    }

    @Test
    public void updateUserWithSameEmailTest() {
        User user1 = createUser("Ivan Ivanov", "ivan@example.com");
        User user2 = createUser("Petr Petrov", "petr@example.com");

        UserDto updatedDataUser = new UserDto();
        updatedDataUser.setEmail("ivan@example.com");

        assertThrows(DuplicatedDataException.class, () -> userService.update(user2.getId(), updatedDataUser));
    }

    @Test
    public void updateUserNotFoundExceptionTestt() {
        UserDto userDto = new UserDto();
        userDto.setEmail("ivan@example.com");
        userDto.setName("Ivan Ivanov");

        assertThrows(NotFoundException.class, () -> userService.update(99L, userDto));
    }

    @Test
    public void findUserByIdTest() {
        User user = createUser("Ivan Ivanov", "ivan@example.com");

        UserDto foundUser = userService.findById(user.getId());

        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getName()).isEqualTo("Ivan Ivanov");
        assertThat(foundUser.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    public void findUserByIdNotFoundExceptionTest() {
        assertThrows(NotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    public void findAllUsersTest() {
        User user1 = createUser("Ivan Ivanov", "ivan@example.com");
        User user2 = createUser("Petr Petrov", "petr@example.com");

        List<UserDto> users = new ArrayList<>(userService.findAll());

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getId()).isEqualTo(user1.getId());
        assertThat(users.get(1).getId()).isEqualTo(user2.getId());
    }

    @Test
    public void deleteUserTest() {
        User user = createUser("Ivan Ivanov", "ivan@example.com");

        userService.delete(user.getId());

        assertThrows(NotFoundException.class, () -> userService.findById(user.getId()));
    }

    private User createUser(String name, String email) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .build());
    }

}
