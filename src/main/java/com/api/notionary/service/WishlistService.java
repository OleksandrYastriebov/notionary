package com.api.notionary.service;

import com.api.notionary.dto.WishlistDto;
import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.exception.WishlistNotFoundException;
import com.api.notionary.helper.Mapper;
import com.api.notionary.repository.UserRepository;
import com.api.notionary.repository.WishListRepository;
import com.api.notionary.repository.WishlistItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final WishListItemService wishListItemService;
    private final Mapper mapper;

    @Autowired
    public WishlistService(WishListRepository wishListRepository, UserRepository userRepository,
                           WishListItemService wishListItemService, Mapper mapper) {
        this.wishListRepository = wishListRepository;
        this.userRepository = userRepository;
        this.wishListItemService = wishListItemService;
        this.mapper = mapper;
    }

    public WishlistDto findWishlistById(String wishlistId) {
        WishList wishlist = wishListRepository
                .findById(wishlistId)
                .orElseThrow(() -> new WishlistNotFoundException(String.format("Wishlist with id %s can not be found.", wishlistId)));
        return mapper.mapWishlistToDto(wishlist);
    }

    public WishlistDto findWishlistWithItemsById(String wishlistId) {
        WishList wishlist = wishListRepository
                .findById(wishlistId)
                .orElseThrow(() -> new WishlistNotFoundException(String.format("Wishlist with id %s can not be found.", wishlistId)));
        WishlistDto wishlistDto = mapper.mapWishlistToDto(wishlist);
        List<WishlistItemDto> wishlistItemDtos = wishListItemService.findAllWishlistItemsById(wishlistId);
        wishlistDto.setWishListItems(wishlistItemDtos);
        return wishlistDto;
    }

    @Transactional
    public WishlistDto saveWishlist(WishlistDto wishlistDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException(String.format("User %s not found in repository.", userEmail)));
        wishlistDto.setUser(user);
        WishList wishlist = mapper.mapWishlistToEntity(wishlistDto);
        return mapper.mapWishlistToDto(wishListRepository.save(wishlist));
    }

    public List<WishlistDto> getWishlistsForUser(User user) {
        List<WishList> wisList = wishListRepository.findByUser(user);
        return wisList.stream().map(item -> {
            WishlistDto wishlistDto = new WishlistDto();
            wishlistDto.setId(item.getId());
            wishlistDto.setIsPublic(item.getIsPublic());
            wishlistDto.setTitle(item.getTitle());
            wishlistDto.setCreatedAt(item.getCreatedAt());
            return wishlistDto;
        }).toList();
    }

    public boolean isWishlistOwner(String wishlistId, String userEmail) {
        WishlistDto wishlistDto = findWishlistById(wishlistId);
        return userEmail.equals(wishlistDto.getUser().getEmail());
    }

    public boolean isPublic(String wishlistId) {
        WishlistDto wishlistDto = findWishlistById(wishlistId);
        return wishlistDto.getIsPublic();
    }

    @Transactional
    public void deleteWishList(String wishlistId) {
        if (findWishlistById(wishlistId) == null) {
            throw new WishlistNotFoundException(
                    (String.format("*ERROR* Trying to delete wishlist with id %s. Wishlist not found", wishlistId)));
        }
        wishListRepository.deleteById(wishlistId);
    }

    @Transactional
    public void changeVisibility(String wishlistId, Boolean isPublic) {
        wishListRepository.updateVisibility(wishlistId, isPublic);
    }

    @Transactional
    public void changeTitle(String wishlistId, String title) {
        wishListRepository.updateTitle(wishlistId, title);
    }
}
