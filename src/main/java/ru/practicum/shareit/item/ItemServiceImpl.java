package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoInput;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto add(ItemDto itemDto, Long ownerId) {
        if (ownerId == null) {
            throw new NotFoundException("ID владельца не может быть null");
        }
        User owner = userRepository.findById(ownerId).orElseThrow(()
                -> new NotFoundException("Пользователь с id = " + ownerId + " не найден"));

        Item item = itemMapper.mapToItem(itemDto, owner);
        Item newItem = itemRepository.save(item);
        return itemMapper.mapToItemDto(newItem);
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemMapper.mapToItemDto(itemRepository.search(text));
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id = " + itemId + " не найден"));

        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");
        Sort sortByStartAsc = Sort.by(Sort.Direction.ASC, "start");
        ItemDto itemDto = itemMapper.mapToItemDto(item);


        List<Comment> comments = commentRepository.findByItem_Id(itemId);
        itemDto.setComments(commentMapper.mapToCommentDtoList(comments));


        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository.findFirstByItem_IdAndStatusAndStartLessThanEqual(itemId, Status.APPROVED, now, sortByStartDesc)
                    .ifPresent(b -> itemDto.setLastBooking(bookingMapper.mapToBookingShortDto(b)));

            bookingRepository.findFirstByItem_IdAndStatusAndStartGreaterThan(itemId, Status.APPROVED, now, sortByStartAsc)
                    .ifPresent(b -> itemDto.setNextBooking(bookingMapper.mapToBookingShortDto(b)));
        }

        return itemDto;
    }

    @Override
    public Collection<ItemDto> findAllByOwnerId(Long ownerId) {
        Collection<Item> items = itemRepository.findAllByOwnerId(ownerId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        Sort sortByStartDesc = Sort.by(Sort.Direction.DESC, "start");

        List<Booking> allBookings = bookingRepository.findByItem_IdInAndStatus(itemIds, Status.APPROVED, sortByStartDesc);
        Map<Long, List<Booking>> bookingsByItem = allBookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        List<Comment> allComments = commentRepository.findByItem_IdIn(itemIds);
        Map<Long, List<Comment>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            ItemDto dto = itemMapper.mapToItemDto(item);

            List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());
            dto.setComments(commentMapper.mapToCommentDtoList(itemComments));

            List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());

            itemBookings.stream()
                    .filter(b -> !b.getStart().isAfter(now))
                    .findFirst()
                    .ifPresent(b -> dto.setLastBooking(bookingMapper.mapToBookingShortDto(b)));

            itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .reduce((first, second) -> second)
                    .ifPresent(b -> dto.setNextBooking(bookingMapper.mapToBookingShortDto(b)));

            result.add(dto);
        }

        return result;
    }

    @Override
    public void delete(long id) {
        itemRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long ownerId, Long itemId) {
        if (ownerId == null) {
            throw new NotFoundException("ID владельца не может быть null");
        }
        userService.findById(ownerId);

        Item newItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id = " + itemId + " не найден"));

        if (!newItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь с id = " + ownerId + " не является владельцем предмета с id = " + itemId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            newItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            newItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            newItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(newItem);
        return itemMapper.mapToItemDto(updatedItem);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDtoInput commentDtoInput) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));


        boolean hasCompletedBooking = bookingRepository
                .existsByBooker_IdAndItem_IdAndStatusAndStartLessThanEqual(userId, itemId, Status.APPROVED, LocalDateTime.now());

        if (!hasCompletedBooking) {
            throw new ValidationException("Оставить отзыв может только арендатор с завершённым бронированием");
        }

        Comment comment = new Comment();
        comment.setText(commentDtoInput.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.saveAndFlush(comment);
        return commentMapper.mapToCommentDto(savedComment);
    }
}
