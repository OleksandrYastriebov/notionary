package com.api.notionary.service;

import com.api.notionary.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlist.UpdateWishlistRequest;
import com.api.notionary.dto.wishlist.WishListContainerDto;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.WishlistNotFoundException;
import com.api.notionary.repository.WishListAccessRepository;
import com.api.notionary.repository.WishListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishListServiceTest {

    private static final int MAX_WISHLISTS = 10;

    @Mock
    private WishListRepository wishListRepository;
    @Mock
    private WishListAccessRepository wishlistAccessRepository;

    @InjectMocks
    private WishListService wishListService;

    private User user;
    private WishList wishList;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wishListService, "maxWishlistsPerAccount", MAX_WISHLISTS);
        user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);
        wishList = new WishList(user, "My List", false, null);
        wishList.setId("wl-123");
    }

    @Test
    void createWishlist_shouldSaveAndReturnDto_whenUnderLimit() {
        when(wishListRepository.countByUser(user)).thenReturn(2);
        when(wishListRepository.save(any(WishList.class))).thenAnswer(inv -> {
            WishList wl = inv.getArgument(0);
            wl.setId("new-id");
            return wl;
        });
        CreateWishlistRequest request = new CreateWishlistRequest("New List", true, null);

        WishListDto result = wishListService.createWishlist(request, user);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("New List");
        assertThat(result.isPublic()).isTrue();
        verify(wishListRepository).save(any(WishList.class));
    }

    @Test
    void createWishlist_shouldThrow_whenMaxLimitReached() {
        when(wishListRepository.countByUser(user)).thenReturn(MAX_WISHLISTS);
        CreateWishlistRequest request = new CreateWishlistRequest("New List", false, null);

        assertThatThrownBy(() -> wishListService.createWishlist(request, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Maximum limit");
        verify(wishListRepository, never()).save(any());
    }

    @Test
    void deleteWishList_shouldDelete_whenOwner() {
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));

        wishListService.deleteWishList("wl-123", user);

        verify(wishListRepository).delete(wishList);
    }

    @Test
    void deleteWishList_shouldThrow_whenNotOwner() {
        User otherUser = new User("Jane", "Doe", "jane@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        otherUser.setId(2L);
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));

        assertThatThrownBy(() -> wishListService.deleteWishList("wl-123", otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owner");
    }

    @Test
    void updateWishlist_shouldUpdateAndReturnDto() {
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));
        UpdateWishlistRequest request = new UpdateWishlistRequest("Updated Title", true, "https://img.url");

        WishListDto result = wishListService.updateWishlist("wl-123", request, user);

        assertThat(wishList.getTitle()).isEqualTo("Updated Title");
        assertThat(wishList.getIsPublic()).isTrue();
        assertThat(wishList.getImageUrl()).isEqualTo("https://img.url");
        assertThat(result.title()).isEqualTo("Updated Title");
    }

    @Test
    void findWishlistById_shouldReturnDto_whenPublic() {
        wishList.setIsPublic(true);
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));

        WishListDto result = wishListService.findWishlistById("wl-123", null);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("wl-123");
        assertThat(result.title()).isEqualTo("My List");
    }

    @Test
    void findWishlistById_shouldThrow_whenPrivateAndUserNull() {
        wishList.setIsPublic(false);
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));

        assertThatThrownBy(() -> wishListService.findWishlistById("wl-123", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("log in");
    }

    @Test
    void findWishlistById_shouldReturnDto_whenOwner() {
        wishList.setIsPublic(false);
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));

        WishListDto result = wishListService.findWishlistById("wl-123", user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("wl-123");
    }

    @Test
    void findWishlistById_shouldReturnDto_whenHasAccess() {
        wishList.setIsPublic(false);
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishList, "friend@example.com"))
                .thenReturn(true);
        User friend = new User("Friend", "User", "friend@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        friend.setId(2L);

        WishListDto result = wishListService.findWishlistById("wl-123", friend);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("wl-123");
    }

    @Test
    void findWishlistById_shouldThrow_whenPrivateAndNoAccess() {
        wishList.setIsPublic(false);
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));
        when(wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishList, "stranger@example.com"))
                .thenReturn(false);
        User stranger = new User("Stranger", "User", "stranger@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        stranger.setId(3L);

        assertThatThrownBy(() -> wishListService.findWishlistById("wl-123", stranger))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("permissions");
    }

    @Test
    void getWishlistsForUser_shouldReturnContainer() {
        when(wishListRepository.findByUser(user)).thenReturn(List.of(wishList));

        WishListContainerDto result = wishListService.getWishlistsForUser(user);

        assertThat(result.wishLists()).hasSize(1);
        assertThat(result.wishLists().getFirst().title()).isEqualTo("My List");
    }

    @Test
    void getWishlistEntityForOwner_shouldReturnEntity_whenOwner() {
        when(wishListRepository.findById("wl-123")).thenReturn(Optional.of(wishList));

        WishList result = wishListService.getWishlistEntityForOwner("wl-123", user);

        assertThat(result).isSameAs(wishList);
    }

    @Test
    void findWishlistById_shouldThrow_whenWishlistNotFound() {
        when(wishListRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishListService.findWishlistById("missing", user))
                .isInstanceOf(WishlistNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
