package com.api.notionary.controller;

import com.api.notionary.controller.docs.ApiForbiddenErrorDoc;
import com.api.notionary.controller.docs.ApiNotFoundErrorDoc;
import com.api.notionary.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.comment.CommentContainerDto;
import com.api.notionary.dto.comment.CommentDto;
import com.api.notionary.dto.payload.request.comment.CreateCommentRequest;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.CommentService;
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