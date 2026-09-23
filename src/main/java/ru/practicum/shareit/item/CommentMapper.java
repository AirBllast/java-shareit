package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public CommentDto mapToCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public List<CommentDto> mapToCommentDtoList(List<Comment> comments) {
        if (comments == null) {
            return List.of();
        }
        List<CommentDto> result = new ArrayList<>();

        for (Comment comment : comments) {
            result.add(mapToCommentDto(comment));
        }

        return result;
    }
}
