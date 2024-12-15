package com.api.notionary.service;

import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.repository.WishListRepository;
import com.api.notionary.repository.WishlistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WishListItemService {

    private WishlistItemRepository wishlistItemRepository;
    private WishListRepository wishListRepository;

    @Autowired
    public WishListItemService(WishlistItemRepository wishlistItemRepository, WishListRepository wishListRepository) {
        this.wishListRepository = wishListRepository;
        this.wishlistItemRepository = wishlistItemRepository;
    }

    public WishlistItemDto addWishListItem(String wishlistId, WishlistItemDto wishlistItemDto) {
        return null;
    }
}
