package com.api.wishoria.service;

import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.access.AccessesContainerDto;
import com.api.wishoria.dto.payload.request.wishlist.RevokeAccessRequest;
import com.api.wishoria.dto.payload.request.wishlist.ShareWishListRequest;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import com.api.wishoria.entity.WishlistAccess;
import com.api.wishoria.entity.UserRole;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.repository.UserRepository;
import com.api.wishoria.repository.WishListAccessRepository;
import com.api.wishoria.repository.WishListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishListAccessServiceTest {

    private static final int MAX_SHARES = 10;

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private WishListAccessRepository wishlistAccessRepository;
    @Mock
    private WishListRepository wishListRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WishListAccessService wishListAccessService;

    private User owner;
    private WishList wishList;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wishListAccessService, "maxShares", MAX_SHARES);
        owner = new User("Owner", "User", "owner@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        owner.setId(1L);
        wishList = new WishList(owner, "My List", false, null);
        wishList.setId("wl-1");
    }

    @Test
    void getAllGrantedEmailsForWishlist_shouldReturnContainer() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.findEmailsByWishlistId("wl-1")).thenReturn(List.of("a@x.com", "b@x.com"));

        AccessesContainerDto result = wishListAccessService.getAllGrantedEmailsForWishlist("wl-1", owner);

        assertThat(result.emails()).containsExactly("a@x.com", "b@x.com");
    }

    @Test
    void getAllGrantedEmailsForWishlist_shouldThrow_whenNotOwner() {
        User other = new User("Other", "User", "other@example.com", "pass",
                Instant.now(), UserRole.ROLE_USER);
        other.setId(2L);
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));

        assertThatThrownBy(() -> wishListAccessService.getAllGrantedEmailsForWishlist("wl-1", other))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void getAllGrantedEmailsForWishlist_shouldThrow_whenUserNull() {
        assertThatThrownBy(() -> wishListAccessService.getAllGrantedEmailsForWishlist("wl-1", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Authentication");
    }

    @Test
    void grantAccess_shouldSaveAndPublishEvent() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishList, "friend@example.com"))
                .thenReturn(false);
        when(wishlistAccessRepository.countByWishListId("wl-1")).thenReturn(2);
        when(userRepository.existsByEmail("friend@example.com")).thenReturn(true);
        when(wishlistAccessRepository.save(any(WishlistAccess.class))).thenAnswer(inv -> inv.getArgument(0));
        ShareWishListRequest request = new ShareWishListRequest("friend@example.com");

        ApiResponseWrapper result = wishListAccessService.grantAccess("wl-1", request, owner);

        assertThat(result.message()).contains("friend@example.com");
        ArgumentCaptor<WishlistAccess> captor = ArgumentCaptor.forClass(WishlistAccess.class);
        verify(wishlistAccessRepository).save(captor.capture());
        assertThat(captor.getValue().getGrantedUserEmail()).isEqualTo("friend@example.com");
        assertThat(captor.getValue().getWishList()).isSameAs(wishList);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void grantAccess_shouldThrow_whenSharingWithSelf() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        ShareWishListRequest request = new ShareWishListRequest("owner@example.com");

        assertThatThrownBy(() -> wishListAccessService.grantAccess("wl-1", request, owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("yourself");
        verify(wishlistAccessRepository, never()).save(any());
    }

    @Test
    void grantAccess_shouldThrow_whenAlreadyHasAccess() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishList, "friend@example.com"))
                .thenReturn(true);
        ShareWishListRequest request = new ShareWishListRequest("friend@example.com");

        assertThatThrownBy(() -> wishListAccessService.grantAccess("wl-1", request, owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already has access");
    }

    @Test
    void grantAccess_shouldThrow_whenMaxSharesReached() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishList, "friend@example.com"))
                .thenReturn(false);
        when(wishlistAccessRepository.countByWishListId("wl-1")).thenReturn(MAX_SHARES);
        ShareWishListRequest request = new ShareWishListRequest("friend@example.com");

        assertThatThrownBy(() -> wishListAccessService.grantAccess("wl-1", request, owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Maximum limit");
    }

    @Test
    void revokeAccess_shouldDeleteAndReturnMessage() {
        WishlistAccess access = new WishlistAccess(wishList, "revoke@example.com");
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.findByWishListAndGrantedUserEmail(wishList, "revoke@example.com"))
                .thenReturn(Optional.of(access));
        RevokeAccessRequest request = new RevokeAccessRequest("revoke@example.com");

        ApiResponseWrapper result = wishListAccessService.revokeAccess("wl-1", request, owner);

        assertThat(result.message()).contains("revoke@example.com");
        verify(wishlistAccessRepository).delete(access);
    }

    @Test
    void revokeAccess_shouldThrow_whenAccessNotFound() {
        when(wishListRepository.findById("wl-1")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.findByWishListAndGrantedUserEmail(wishList, "nobody@example.com"))
                .thenReturn(Optional.empty());
        RevokeAccessRequest request = new RevokeAccessRequest("nobody@example.com");

        assertThatThrownBy(() -> wishListAccessService.revokeAccess("wl-1", request, owner))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Access record not found");
    }

    @Test
    void grantAccess_shouldThrow_whenWishlistNotFound() {
        when(wishListRepository.findById("wl-missing")).thenReturn(Optional.empty());
        ShareWishListRequest request = new ShareWishListRequest("friend@example.com");

        assertThatThrownBy(() -> wishListAccessService.grantAccess("wl-missing", request, owner))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Wishlist not found");
    }
}
