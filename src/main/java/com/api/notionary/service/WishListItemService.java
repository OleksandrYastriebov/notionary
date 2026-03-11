package com.api.notionary.service;

import com.api.notionary.dto.payload.request.wishlistitem.CreateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.UpdateWishListItemRequest;
import com.api.notionary.dto.payload.request.wishlistitem.WishlistItemIsCheckedRequest;
import com.api.notionary.dto.wishlistitem.WishListItemDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import com.api.notionary.exception.WishlistItemNotFoundException;
import com.api.notionary.repository.WishListItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class WishListItemService {

    private final WishListItemRepository wishListItemRepository;
    private final WishListService wishListService;

    public WishListItemDto findWishlistItemByIdAndWishlistId(String wishlistId, String itemId, User user) {
        wishListService.findWishlistById(wishlistId, user);
        WishListItem wishlistItem = getWishlistItem(wishlistId, itemId);
        return wishlistItem.toDto();
    }

    @Transactional
    public WishListItemDto createWishListItem(String wishlistId, CreateWishListItemRequest createWishListItemRequest, User user) {
        WishList wishList = wishListService.getWishlistEntityForOwner(wishlistId, user);
        return wishListItemRepository.save(createWishListItemRequest.toEntity(wishList)).toDto();
    }

    @Transactional
    public void deleteWishlistItem(String wishlistId, String itemId, User user) {
        wishListService.getWishlistEntityForOwner(wishlistId, user);
        WishListItem wishListItem = getWishlistItem(wishlistId, itemId);
        wishListItemRepository.delete(wishListItem);
    }

    @Transactional
    public WishListItemDto updateWishlistItem(UpdateWishListItemRequest updateWishListItemRequest, String wishlistId,
                                              String itemId, User user) {
        wishListService.getWishlistEntityForOwner(wishlistId, user);
        WishListItem wishlistItem = getWishlistItem(wishlistId, itemId);
        updateWishListItemRequest.updateEntity(wishlistItem);
        return wishlistItem.toDto();
    }

    @Transactional
    public void toggleIsChecked(String wishlistId, String itemId, WishlistItemIsCheckedRequest request, User user) {
        wishListService.findWishlistById(wishlistId, user);
        WishListItem wishlistItem = getWishlistItem(wishlistId, itemId);
        wishlistItem.setIsChecked(request.getIsChecked());
    }

    private @NonNull WishListItem getWishlistItem(String wishlistId, String itemId) {
        return wishListItemRepository.findByIdAndWishListId(itemId, wishlistId).orElseThrow(() ->
                new WishlistItemNotFoundException(String.format("Wishlist item with id: %s and wishlistId: %s was not found.", itemId, wishlistId)));
    }

}

