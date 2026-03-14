package com.api.notionary.service;

import com.api.notionary.dto.comment.CommentContainerDto;
import com.api.notionary.dto.comment.CommentDto;
import com.api.notionary.dto.payload.request.comment.CreateCommentRequest;
import com.api.notionary.entity.Comment;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import com.api.notionary.exception.EntityNotFoundException;
import com.api.notionary.exception.WishlistItemNotFoundException;
import com.api.notionary.exception.WishlistNotFoundException;
import com.api.notionary.repository.CommentRepository;
import com.api.notionary.repository.WishListItemRepository;
import com.api.notionary.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final WishListItemRepository wishListItemRepository;
    private final WishListRepository wishListRepository;
    private final WishListService wishListService;

    public CommentContainerDto getCommentsForItem(String wishlistId, String itemId, User user) {
        if (isWishlistOwner(wishlistId, user)) {
            return new CommentContainerDto(Collections.emptyList());
        }

        WishListItem item = validateAccessAndFetchItem(wishlistId, itemId, user);

        List<CommentDto> comments = commentRepository.findByWishListItemOrderByCreatedAtAsc(item)
                .stream()
                .map(CommentDto::fromEntity)
                .toList();

        return new CommentContainerDto(comments);
    }

    @Transactional
    public CommentDto createComment(String wishlistId, String itemId, CreateCommentRequest request, User user) {
        requireAuthenticatedUser(user);

        if (isWishlistOwner(wishlistId, user)) {
            throw new AccessDeniedException("Owners cannot participate in surprise discussions.");
        }

        WishListItem wishListItem = validateAccessAndFetchItem(wishlistId, itemId, user);

        Comment comment = new Comment(request.text(), wishListItem, user);

        return CommentDto.fromEntity(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(String wishlistId, String itemId, Long commentId, User user) {
        requireAuthenticatedUser(user);

        if (isWishlistOwner(wishlistId, user)) {
            throw new AccessDeniedException("Owners cannot manage surprise discussions.");
        }

        validateAccessAndFetchItem(wishlistId, itemId, user);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found."));

        if (!comment.getWishListItem().getId().equals(itemId)) {
            throw new IllegalArgumentException("This comment does not belong to the specified item.");
        }

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only delete your own comments.");
        }

        commentRepository.delete(comment);
    }

    private boolean isWishlistOwner(String wishlistId, User user) {
        if (user == null) {
            return false;
        }
        WishList wishList = wishListRepository.findById(wishlistId)
                .orElseThrow(() -> new WishlistNotFoundException("Wishlist not found."));

        return wishList.getUser().getId().equals(user.getId());
    }

    private WishListItem validateAccessAndFetchItem(String wishlistId, String itemId, User user) {
        wishListService.findWishlistById(wishlistId, user);

        return wishListItemRepository.findByIdAndWishListId(itemId, wishlistId)
                .orElseThrow(() -> new WishlistItemNotFoundException("Wishlist item not found."));
    }

    private void requireAuthenticatedUser(User user) {
        if (user == null) {
            throw new AccessDeniedException("You must be logged in to perform this action.");
        }
    }
}