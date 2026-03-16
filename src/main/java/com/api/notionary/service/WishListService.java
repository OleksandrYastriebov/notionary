package com.api.notionary.service;

import com.api.notionary.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.notionary.dto.payload.request.wishlist.UpdateWishlistRequest;
import com.api.notionary.dto.wishlist.WishListContainerDto;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.exception.WishlistNotFoundException;
import com.api.notionary.repository.WishListAccessRepository;
import com.api.notionary.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WishListService {

    @Value("${app.limits.max-wishlists}")
    private int maxWishlistsPerAccount;

    private final WishListRepository wishListRepository;
    private final WishListAccessRepository wishlistAccessRepository;

    @Transactional
    public WishListDto createWishlist(CreateWishlistRequest createWishlistRequest, User user) {
        if (wishListRepository.countByUser(user) >= maxWishlistsPerAccount) {
            throw new IllegalStateException("Maximum limit WishLists per account reached.");
        }

        WishList wishlist = createWishlistRequest.toEntity(user);
        return wishListRepository.save(wishlist).toDto();
    }

    @Transactional
    public void deleteWishList(String wishlistId, User user) {
        WishList wishList = getWishlistEntityForOwner(wishlistId, user);
        wishListRepository.delete(wishList);
    }

    @Transactional
    public WishListDto updateWishlist(String wishlistId, UpdateWishlistRequest request, User user) {
        WishList wishList = getWishlistEntityForOwner(wishlistId, user);
        request.updateEntity(wishList);
        return wishList.toDto();
    }

    public WishListDto findWishlistById(String wishlistId, User user) {
        WishList wishlist = getWishlistById(wishlistId);
        if (Boolean.TRUE.equals(wishlist.getIsPublic())) {
            return wishlist.toDto();
        }

        if (user == null) {
            throw new AccessDeniedException("This is a private wishlist. Please, log in.");
        }

        String userEmail = user.getEmail().toLowerCase().trim();

        if (wishlist.getUser().getEmail().equalsIgnoreCase(user.getEmail())) {
            return wishlist.toDto();
        }

        if (wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishlist, userEmail)) {
            return wishlist.toDto();
        }
        throw new AccessDeniedException("This is a private wishlist. You don't have permissions to see it.");
    }

    public WishListContainerDto getWishlistsForUser(User user) {
        List<WishListDto> wisLists = wishListRepository.findByUser(user).stream()
                .map(WishList::toDto)
                .toList();
        return new WishListContainerDto(wisLists);
    }

    private WishList getWishlistById(String wishlistId) {
        return wishListRepository.findById(wishlistId)
                .orElseThrow(() -> new WishlistNotFoundException(String.format("Wishlist with id %s can not be found.", wishlistId)));
    }

    public WishList getWishlistEntityForOwner(String wishlistId, User user) {
        WishList wishList = getWishlistById(wishlistId);
        checkOwnership(user, wishList);
        return wishList;
    }

    public List<WishListDto> getPublicWishlistsForUser(Long userId) {
        return wishListRepository.findByUserIdAndIsPublicTrue(userId).stream()
                .map(WishList::toDto)
                .toList();
    }

    private void checkOwnership(User user, WishList wishlist) {
        if (user == null) {
            throw new AccessDeniedException("You need to be logged in to modify this wishlist.");
        }
        if (!isWishlistOwner(wishlist, user.getEmail())) {
            throw new AccessDeniedException("You need to be the owner to modify this wishlist.");
        }
    }

    private boolean isWishlistOwner(WishList wishList, String userEmail) {
        return userEmail.trim().equalsIgnoreCase(wishList.getUser().getEmail().trim());
    }
}
