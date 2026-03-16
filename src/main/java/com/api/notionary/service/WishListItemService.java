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
import com.api.notionary.exception.WishlistItemNotFoundException;
import com.api.notionary.repository.WishListItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WishListItemService {

    @Value("${app.limits.max-items-per-list}")
    private int maxWishlistsPerWishlist;

    private final WishListItemRepository wishListItemRepository;
    private final WishListService wishListService;

    public WishListItemDto findWishlistItemByIdAndWishlistId(String wishListId, String itemId, User user) {
        wishListService.findWishlistById(wishListId, user);
        WishListItem wishlistItem = getWishlistItem(wishListId, itemId);
        return wishlistItem.toDto();
    }

    public WishListItemContainerDto findAllWishListItemsForWishList(String wishListId, User user) {
        WishListDto wishList = wishListService.findWishlistById(wishListId, user);
        return new WishListItemContainerDto(wishList.wishListItems());
    }

    @Transactional
    public WishListItemDto createWishListItem(String wishListId, CreateWishListItemRequest createWishListItemRequest, User user) {
        WishListDto wishListDto = wishListService.findWishlistById(wishListId, user);

        if (wishListDto.wishListItems().size() >= maxWishlistsPerWishlist) {
            throw new IllegalStateException("Maximum Wishlist item limit per reached.");
        }

        WishList wishList = wishListService.getWishlistEntityForOwner(wishListId, user);
        return wishListItemRepository.save(createWishListItemRequest.toEntity(wishList)).toDto();
    }

    @Transactional
    public void deleteWishlistItem(String wishListId, String itemId, User user) {
        wishListService.getWishlistEntityForOwner(wishListId, user);
        WishListItem wishListItem = getWishlistItem(wishListId, itemId);
        wishListItemRepository.delete(wishListItem);
    }

    @Transactional
    public WishListItemDto updateWishlistItem(UpdateWishListItemRequest updateWishListItemRequest, String wishListId,
                                              String itemId, User user) {
        wishListService.getWishlistEntityForOwner(wishListId, user);
        WishListItem wishlistItem = getWishlistItem(wishListId, itemId);
        updateWishListItemRequest.updateEntity(wishlistItem);
        return wishlistItem.toDto();
    }

    @Transactional
    public void toggleIsChecked(String wishlistId, String itemId, WishlistItemIsCheckedRequest request, User user) {
        if (user == null) {
            throw new AccessDeniedException("You must be logged in to reserve items.");
        }
        wishListService.findWishlistById(wishlistId, user);
        WishListItem item = getWishlistItem(wishlistId, itemId);

        if (request.isChecked()) {
            reserve(item, user);
        } else {
            unreserve(item, user);
        }

    }

    private void reserve(WishListItem item, User user) {
        item.setChecked(true);
        item.setCheckedBy(user);
    }

    private void unreserve(WishListItem item, User user) {
        User reserver = item.getCheckedBy();
        boolean reservedByAnotherUser = reserver != null && !reserver.getId().equals(user.getId());
        boolean callerIsWishlistOwner = item.getWishList().getUser().getId().equals(user.getId());

        if (reservedByAnotherUser && !callerIsWishlistOwner) {
            throw new AccessDeniedException("You cannot unreserve an item reserved by another user.");
        }
        item.setChecked(false);
        item.setCheckedBy(null);
    }

    private @NonNull WishListItem getWishlistItem(String wishListId, String itemId) {
        return wishListItemRepository.findByIdAndWishListId(itemId, wishListId).orElseThrow(() ->
                new WishlistItemNotFoundException(String.format("Wishlist item with id: %s and wishlistId: %s was not found.", itemId, wishListId)));
    }

}

