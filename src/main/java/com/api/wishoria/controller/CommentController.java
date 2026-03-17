package com.api.wishoria.controller;

import com.api.wishoria.controller.docs.ApiForbiddenErrorDoc;
import com.api.wishoria.controller.docs.ApiNotFoundErrorDoc;
import com.api.wishoria.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.comment.CommentContainerDto;
import com.api.wishoria.dto.comment.CommentDto;
import com.api.wishoria.dto.payload.request.comment.CreateCommentRequest;
import com.api.wishoria.entity.User;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Comments", description = "Endpoints for managing secret discussions on wishlist items")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/wishlists/{wishlistId}/wishes/{itemId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Get all comments", description = "Retrieves all comments for a specific item. Returns an empty list or 403 if the user is the owner of the wishlist.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.DEFAULT)
    @GetMapping
    public ResponseEntity<CommentContainerDto> getCommentsForItem(@PathVariable String wishlistId,
                                                                  @PathVariable String itemId,
                                                                  @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(commentService.getCommentsForItem(wishlistId, itemId, user));
    }

    @Operation(summary = "Create a comment", description = "Adds a comment to an item. The wishlist owner cannot perform this action.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable String wishlistId,
                                                    @PathVariable String itemId,
                                                    @Valid @RequestBody CreateCommentRequest request,
                                                    @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(commentService.createComment(wishlistId, itemId, request, user));
    }

    @Operation(summary = "Delete a comment", description = "Deletes a specific comment. Only the author of the comment can delete it.")
    @ApiUnauthorizedErrorDoc
    @ApiNotFoundErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.MUTATION)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseWrapper> deleteComment(@PathVariable String wishlistId,
                                                            @PathVariable String itemId,
                                                            @PathVariable Long commentId,
                                                            @AuthenticationPrincipal User user) {

        commentService.deleteComment(wishlistId, itemId, commentId, user);
        return ResponseEntity.ok(new ApiResponseWrapper("Comment deleted successfully"));
    }
}