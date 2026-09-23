package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(Long bookerId, Sort sort);

    List<Booking> findByBooker_IdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBooker_IdAndStartIsBeforeAndEndIsAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, Status status, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByItem_Owner_IdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, Status status, Sort sort);

    List<Booking> findByItem_IdInAndStatus(List<Long> itemIds, Status status, Sort sort);

    boolean existsByBooker_IdAndItem_IdAndStatusAndStartLessThanEqual(Long bookerId, Long itemId, Status status, LocalDateTime now);

    Optional<Booking> findFirstByItem_IdAndStatusAndStartLessThanEqual(Long itemId, Status status, LocalDateTime now, Sort sort);

    Optional<Booking> findFirstByItem_IdAndStatusAndStartGreaterThan(Long itemId, Status status, LocalDateTime now, Sort sort);
}
