package com.api.notionary.service;

import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.dto.WishlistItemIsCheckedRequestDto;
import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import com.api.notionary.exception.WishlistItemNotFoundException;
import com.api.notionary.exception.WishlistNotFoundException;
import com.api.notionary.helper.Mapper;
import com.api.notionary.repository.WishListRepository;
import com.api.notionary.repository.WishlistItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishListItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WishListItemService.class);

    private final WishlistItemRepository wishlistItemRepository;
    private final WishListRepository wishListRepository;
    private final Mapper mapper;

    @Autowired
    public WishListItemService(WishlistItemRepository wishlistItemRepository, WishListRepository wishListRepository,
                               Mapper mapper) {
        this.wishListRepository = wishListRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.mapper = mapper;
    }

    public WishlistItemDto findWishlistItemByIdAndWishlistId(String wishlistItemId, String wishlistId) {
        WishListItem wishlistItem = wishlistItemRepository.findByIdAndWishListId(wishlistItemId, wishlistId).orElseThrow(() ->
                new WishlistItemNotFoundException(String.format("Wishlist item with id: %s and wishlistId: %s was not found.", wishlistItemId, wishlistId)));
        return mapper.mapWishlistItemToDto(wishlistItem);
    }

    @Transactional
    public WishlistItemDto addWishListItem(String wishlistId, WishlistItemDto wishlistItemDto) {
        WishList wishlist = wishListRepository.findById(wishlistId).orElseThrow(() ->
                new WishlistNotFoundException(String.format("Wishlist with id: %s was not found.", wishlistId)));
        wishlistItemDto.setWishList(wishlist);
        WishListItem wishListItem = mapper.mapWishlistItemToEntity(wishlistItemDto);

        LOGGER.info("Adding a wishlist item: {} to the wishlist: {}", wishlistItemDto, wishlistId);

        return mapper.mapWishlistItemToDto(wishlistItemRepository.save(wishListItem));
    }

    public List<WishlistItemDto> findAllWishlistItemsById(String wishlistId) {
        List<WishListItem> wishListItems = wishlistItemRepository.findAllByWishListId(wishlistId);
        return wishListItems.stream().map(mapper::mapWishlistItemToDto).toList();
    }

    @Transactional
    public void deleteWishlistItem(String wishlistId, String itemId) {
        if (findWishlistItemByIdAndWishlistId(itemId, wishlistId) == null) {
            throw new WishlistItemNotFoundException(
                    String.format("*ERROR* Trying to delete wishlist item with id %s. Wishlist item not found", wishlistId));
        }
        LOGGER.info("Removing wishlist item: {} from the wishlist {}", itemId, wishlistId);

        wishlistItemRepository.deleteByIdAndWishListId(itemId, wishlistId);
    }

    @Transactional
    public WishlistItemDto updateWishlistItem(WishlistItemDto wishlistItemDto, String wishlistId, String itemId) {
        LOGGER.info("Start updating wishlist item {}", itemId);

        WishListItem wishListItem = wishlistItemRepository.findByIdAndWishListId(itemId, wishlistId)
                .orElseThrow(() -> new WishlistItemNotFoundException(
                        String.format("Wishlist item %s in wishlist %s not found", itemId, wishlistId)));

        if (wishlistItemDto.getTitle() != null) {
            wishListItem.setTitle(wishlistItemDto.getTitle());
        }
        if (wishlistItemDto.getPrice() != null) {
            wishListItem.setPrice(wishlistItemDto.getPrice());
        }
        if (wishlistItemDto.getUrl() != null) {
            wishListItem.setUrl(wishlistItemDto.getUrl());
        }
        if (wishlistItemDto.getDescription() != null) {
            wishListItem.setDescription(wishlistItemDto.getDescription());
        }
        if (wishlistItemDto.getIsChecked() != null) {
            wishListItem.setIsChecked(wishlistItemDto.getIsChecked());
        }
        LOGGER.info("Wishlist item {} updated successfully", itemId);

        return mapper.mapWishlistItemToDto(wishListItem);
    }

    @Transactional
    public void updateIsChecked(WishlistItemIsCheckedRequestDto isCheckedDto, String wishlistId, String itemId) {
        LOGGER.info("Updating isChecked to {} in wishlist item {} in wishlist {}.", isCheckedDto.getIsChecked(), itemId, wishlistId);
        wishlistItemRepository.updateIsChecked(itemId, wishlistId, isCheckedDto.getIsChecked());
    }
}

