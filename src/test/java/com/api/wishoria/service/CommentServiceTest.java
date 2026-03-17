package com.api.wishoria.service;

import com.api.wishoria.dto.comment.CommentContainerDto;
import com.api.wishoria.dto.comment.CommentDto;
import com.api.wishoria.dto.payload.request.comment.CreateCommentRequest;
import com.api.wishoria.entity.Comment;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import com.api.wishoria.entity.WishListItem;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.exception.WishlistItemNotFoundException;
import com.api.wishoria.exception.WishlistNotFoundException;
import com.api.wishoria.repository.CommentRepository;
import com.api.wishoria.repository.WishListItemRepository;
import com.api.wishoria.repository.WishListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private WishListItemRepository wishListItemRepository;
    @Mock
    private WishListRepository wishListRepository;
    @Mock
    private WishListService wishListService;

    @InjectMocks
    private CommentService commentService;

    private User owner;
    private User commenter;
    private WishList wishList;
    private WishListItem wishListItem;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "User", "owner@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        owner.setId(1L);
        commenter = new User("Commenter", "User", "commenter@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        commenter.setId(2L);
        wishList = new WishList(owner, "List", false, null);
        wishList.setId("wl-1");
        wishListItem = new WishListItem(wishList, "Item", null, null, null, null);
        wishListItem.setId("item-1");
        comment = new Comment("Hello", wishListItem, commenter);
        comment.setId(100L);
    }

    @Test
    void getCommentsForItem_shouldReturnEmpty_whenOwner() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));

        CommentContainerDto result = commentService.getCommentsForItem("wl-1", "item-1", owner);

        assertThat(result.comments()).isEmpty();
        verify(wishListRepository).findById("wl-1");
    }

    @Test
    void getCommentsForItem_shouldReturnComments_whenNotOwnerAndHasAccess() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", commenter)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        when(commentRepository.findByWishListItemOrderByCreatedAtAsc(wishListItem))
                .thenReturn(List.of(comment));

        CommentContainerDto result = commentService.getCommentsForItem("wl-1", "item-1", commenter);

        assertThat(result.comments()).hasSize(1);
        assertThat(result.comments().getFirst().text()).isEqualTo("Hello");
    }

    @Test
    void getCommentsForItem_shouldThrow_whenWishlistNotFound() {
        when(wishListRepository.findById("wl-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentsForItem("wl-missing", "item-1", owner))
                .isInstanceOf(WishlistNotFoundException.class);
    }

    @Test
    void createComment_shouldSaveAndReturnDto() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", commenter)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(101L);
            return c;
        });
        CreateCommentRequest request = new CreateCommentRequest("New comment");

        CommentDto result = commentService.createComment("wl-1", "item-1", request, commenter);

        assertThat(result).isNotNull();
        assertThat(result.text()).isEqualTo("New comment");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_shouldThrow_whenOwner() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        CreateCommentRequest request = new CreateCommentRequest("Text");

        assertThatThrownBy(() -> commentService.createComment("wl-1", "item-1", request, owner))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Owners cannot participate");
    }

    @Test
    void createComment_shouldThrow_whenUserNull() {
        CreateCommentRequest request = new CreateCommentRequest("Text");

        assertThatThrownBy(() -> commentService.createComment("wl-1", "item-1", request, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("logged in");
    }

    @Test
    void deleteComment_shouldDelete_whenOwnComment() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", commenter)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        commentService.deleteComment("wl-1", "item-1", 100L, commenter);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_shouldThrow_whenCommentNotOwned() {
        User otherUser = new User("Other", "User", "other@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        otherUser.setId(99L);
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", otherUser)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment("wl-1", "item-1", 100L, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("only delete your own");
    }

    @Test
    void deleteComment_shouldThrow_whenCommentNotFound() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", commenter)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment("wl-1", "item-1", 999L, commenter))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void deleteComment_shouldThrow_whenCommentBelongsToDifferentItem() {
        Comment otherComment = new Comment("Other", wishListItem, commenter);
        otherComment.setId(200L);
        WishListItem otherItem = new WishListItem(wishList, "Other", null, null, null, null);
        otherItem.setId("item-2");
        otherComment.setWishListItem(otherItem);
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", commenter)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        when(commentRepository.findById(200L)).thenReturn(Optional.of(otherComment));

        assertThatThrownBy(() -> commentService.deleteComment("wl-1", "item-1", 200L, commenter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void getCommentsForItem_shouldThrow_whenItemNotFound() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishListService.findWishlistById("wl-1", commenter)).thenReturn(null);
        when(wishListItemRepository.findByIdAndWishListId("item-missing", "wl-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentsForItem("wl-1", "item-missing", commenter))
                .isInstanceOf(WishlistItemNotFoundException.class);
    }
}
