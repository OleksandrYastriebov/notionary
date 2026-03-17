package com.api.wishoria.dto.comment;

import com.api.wishoria.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Data Transfer Object for a Comment")
public record CommentDto(
        @Schema(description = "Unique identifier of the comment", example = "1")
        Long id,

        @Schema(description = "Text of the comment", example = "Let's chip in for this!")
        String text,

        @Schema(description = "ID of the comment author", example = "5948b3a5209148968a034a50d66291df")
        Long authorId,

        @Schema(description = "First name of the author", example = "John")
        String authorFirstName,

        @Schema(description = "Last name of the author", example = "Doe")
        String authorLastName,

        @Schema(description = "Avatar URL of the author", example = "https://res.cloudinary.com/...")
        String authorAvatarUrl,

        @Schema(description = "Creation timestamp", example = "2026-03-14T23:24:00Z")
        Instant createdAt
) {
    public static CommentDto fromEntity(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFirstName(),
                comment.getAuthor().getLastName(),
                comment.getAuthor().getAvatarUrl(),
                comment.getCreatedAt()
        );
    }
}