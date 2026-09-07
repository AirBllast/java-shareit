package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;

    @NonNull
    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @NonNull
    @NotBlank(message = "Описание не должно быть пустым")
    private String description;

    @NonNull
    private Boolean available;

    private Long request;
}
