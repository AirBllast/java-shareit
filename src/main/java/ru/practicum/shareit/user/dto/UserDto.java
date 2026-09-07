package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    @NotNull(message = "Имя не должно быть пустым")
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @NotNull(message = "Email не должен быть пустым")
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный email")
    private String email;
}
