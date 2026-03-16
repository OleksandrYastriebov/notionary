package com.api.notionary.service;

import com.api.notionary.dto.payload.request.wishlistitem.CreateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.UpdateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.WishlistItemIsCheckedRequest;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.dto.wishlistitem.WishListItemContainerDto;
import com.api.notionary.dto.wishlistitem.WishListItemDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import com.api.notionary.entity.UserRole;
import com.api.notionary.exception.WishlistItemNotFoundException;
import com.api.notionary.repository.WishListItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishListItemServiceTest {

    private static final int MAX_ITEMS = 50;

    @Mock
    private WishListItemRepository wishListItemRepository;
    @Mock
    private WishListService wishListService;

    @InjectMocks
    private WishListItemService wishListItemService;

    private User user;
    private WishList wishList;
    private WishListItem wishListItem;
    private WishListDto wishListDto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wishListItemService, "maxWishlistsPerWishlist", MAX_ITEMS);
        user = new User("John", "Doe", "john@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        user.setId(1L);
        wishList = new WishList(user, "My List", false, null);
        wishList.setId("wl-1");
        wishListItem = new WishListItem(wishList, "Item", "https://u.url", "desc", BigDecimal.TEN, null);
        wishListItem.setId("item-1");
        wishListDto = new WishListDto("wl-1", 1L, List.of(wishListItem.toDto()), "My List", false, null, LocalDateTime.now());
    }

    @Test
    void findWishlistItemByIdAndWishlistId_shouldReturnDto() {
        when(wishListService.findWishlistById("wl-1", user)).thenReturn(wishListDto);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));

        WishListItemDto result = wishListItemService.findWishlistItemByIdAndWishlistId("wl-1", "item-1", user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("item-1");
        assertThat(result.title()).isEqualTo("Item");
    }

    @Test
    void findAllWishListItemsForWishList_shouldReturnContainer() {
        when(wishListService.findWishlistById("wl-1", user)).thenReturn(wishListDto);

        WishListItemContainerDto result = wishListItemService.findAllWishListItemsForWishList("wl-1", user);

        assertThat(result.wishListItems()).hasSize(1);
        assertThat(result.wishListItems().getFirst().title()).isEqualTo("Item");
    }

    @Test
    void createWishListItem_shouldSaveAndReturnDto() {
        when(wishListService.findWishlistById("wl-1", user)).thenReturn(wishListDto);
        when(wishListService.getWishlistEntityForOwner("wl-1", user)).thenReturn(wishList);
        when(wishListItemRepository.save(any(WishListItem.class))).thenAnswer(inv -> {
            WishListItem item = inv.getArgument(0);
            item.setId("new-item");
            return item;
        });
        CreateWishListItemRequest request = new CreateWishListItemRequest("New Item", null, null, null, null);

        WishListItemDto result = wishListItemService.createWishListItem("wl-1", request, user);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("New Item");
        verify(wishListItemRepository).save(any(WishListItem.class));
    }

    @Test
    void createWishListItem_shouldThrow_whenMaxItemsReached() {
        WishListDto fullList = new WishListDto("wl-1", 1L,
                List.of(wishListItem.toDto(), wishListItem.toDto() /* 50 items in real scenario */),
                "My List", false, null, LocalDateTime.now());
        when(wishListService.findWishlistById("wl-1", user)).thenReturn(fullList);
        ReflectionTestUtils.setField(wishListItemService, "maxWishlistsPerWishlist", 2);
        CreateWishListItemRequest request = new CreateWishListItemRequest("New", null, null, null, null);

        assertThatThrownBy(() -> wishListItemService.createWishListItem("wl-1", request, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void deleteWishlistItem_shouldDelete() {
        when(wishListService.getWishlistEntityForOwner("wl-1", user)).thenReturn(wishList);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));

        wishListItemService.deleteWishlistItem("wl-1", "item-1", user);

        verify(wishListItemRepository).delete(wishListItem);
    }

    @Test
    void updateWishlistItem_shouldUpdateAndReturnDto() {
        when(wishListService.getWishlistEntityForOwner("wl-1", user)).thenReturn(wishList);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        UpdateWishListItemRequest request = new UpdateWishListItemRequest("Updated Title", null, null, null, null, null);

        WishListItemDto result = wishListItemService.updateWishlistItem(request, "wl-1", "item-1", user);

        assertThat(wishListItem.getTitle()).isEqualTo("Updated Title");
        assertThat(result.title()).isEqualTo("Updated Title");
    }

    @Test
    void toggleIsChecked_shouldSetChecked() {
        when(wishListService.findWishlistById("wl-1", user)).thenReturn(wishListDto);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(true);

        wishListItemService.toggleIsChecked("wl-1", "item-1", request, user);

        assertThat(wishListItem.isChecked()).isTrue();
        assertThat(wishListItem.getCheckedBy()).isEqualTo(user);
    }

    @Test
    void toggleIsChecked_reserverCanUnreserveOwnItem() {
        User reserver = new User("Jane", "Doe", "jane@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        reserver.setId(2L);
        wishListItem.setChecked(true);
        wishListItem.setCheckedBy(reserver);
        when(wishListService.findWishlistById("wl-1", reserver)).thenReturn(wishListDto);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(false);

        wishListItemService.toggleIsChecked("wl-1", "item-1", request, reserver);

        assertThat(wishListItem.isChecked()).isFalse();
        assertThat(wishListItem.getCheckedBy()).isNull();
    }

    @Test
    void toggleIsChecked_shouldThrow_whenAnotherUserTriesToUnreserve() {
        User reserver = new User("Jane", "Doe", "jane@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        reserver.setId(2L);
        User otherUser = new User("Bob", "Smith", "bob@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        otherUser.setId(3L);
        wishListItem.setChecked(true);
        wishListItem.setCheckedBy(reserver);
        when(wishListService.findWishlistById("wl-1", otherUser)).thenReturn(wishListDto);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(false);

        assertThatThrownBy(() -> wishListItemService.toggleIsChecked("wl-1", "item-1", request, otherUser))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot unreserve");
    }

    @Test
    void toggleIsChecked_wishlistOwnerCanUnreserveAnyItem() {
        User reserver = new User("Jane", "Doe", "jane@example.com", "pass",
                LocalDateTime.now(), UserRole.ROLE_USER);
        reserver.setId(2L);
        wishListItem.setChecked(true);
        wishListItem.setCheckedBy(reserver);

        when(wishListService.findWishlistById("wl-1", user)).thenReturn(wishListDto);
        when(wishListItemRepository.findByIdAndWishListId("item-1", "wl-1"))
                .thenReturn(Optional.of(wishListItem));
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(false);

        wishListItemService.toggleIsChecked("wl-1", "item-1", request, user);

        assertThat(wishListItem.isChecked()).isFalse();
        assertThat(wishListItem.getCheckedBy()).isNull();
    }

    @Test
    void toggleIsChecked_shouldThrow_whenUserNull() {
        WishlistItemIsCheckedRequest request = new WishlistItemIsCheckedRequest(true);

        assertThatThrownBy(() -> wishListItemService.toggleIsChecked("wl-1", "item-1", request, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("logged in");
    }

    @Test
    void findWishlistItemByIdAndWishlistId_shouldThrow_whenItemNotFound() {
        when(wishListService.findWishlistById("wl-1", user)).thenReturn(wishListDto);
        when(wishListItemRepository.findByIdAndWishListId("item-missing", "wl-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishListItemService.findWishlistItemByIdAndWishlistId("wl-1", "item-missing", user))
                .isInstanceOf(WishlistItemNotFoundException.class)
                .hasMessageContaining("item-missing");
    }
}
