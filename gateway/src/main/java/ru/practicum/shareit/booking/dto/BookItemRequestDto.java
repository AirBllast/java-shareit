package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

	@NotNull
	private Long itemId;

	@NotNull
	@FutureOrPresent
	private LocalDateTime start;

	@NotNull
	@Future
	private LocalDateTime end;

	@AssertTrue(message = "Дата окончания должна быть позже даты начала")
	public boolean isEndAfterStart() {
		if (start == null || end == null) {
			return true;
		}
		return end.isAfter(start);
	}
}
