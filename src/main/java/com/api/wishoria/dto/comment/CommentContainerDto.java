package com.api.wishoria.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Container holding a list of comments")
public record CommentContainerDto(

        @Schema(description = "Array of comments")
        List<CommentDto> comments
) {
}