package com.api.wishoria.service;

import com.api.wishoria.config.CacheConfig;
import com.api.wishoria.dto.payload.request.wishlist.CreateWishlistRequest;
import com.api.wishoria.dto.payload.request.wishlist.UpdateWishlistRequest;
import com.api.wishoria.dto.wishlist.WishListContainerDto;
import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import com.api.wishoria.exception.UserNotFoundException;
import com.api.wishoria.exception.WishlistNotFoundException;
import com.api.wishoria.repository.UserRepository;
import com.api.wishoria.repository.WishListAccessRepository;
import com.api.wishoria.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    private final UserRepository userRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SITEMAP_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.AVAILABLE_WISHLISTS_CACHE, allEntries = true)
    })
    public WishListDto createWishlist(CreateWishlistRequest createWishlistRequest, User user) {
        userRepository.findByIdWithLock(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + user.getId()));

        if (wishListRepository.countByUser(user) >= maxWishlistsPerAccount) {
            throw new IllegalStateException("Maximum limit WishLists per account reached.");
        }

        WishList wishlist = createWishlistRequest.toEntity(user);
        return wishListRepository.save(wishlist).toDto();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SITEMAP_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.AVAILABLE_WISHLISTS_CACHE, allEntries = true)
    })
    public void deleteWishList(String wishlistId, User user) {
        WishList wishList = getWishlistEntityForOwner(wishlistId, user);
        wishListRepository.delete(wishList);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.SITEMAP_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.AVAILABLE_WISHLISTS_CACHE, allEntries = true)
    })
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

    public WishList getWishlistEntityForOwnerWithLock(String wishlistId, User user) {
        WishList wishList = wishListRepository.findByIdWithLock(wishlistId)
                .orElseThrow(() -> new WishlistNotFoundException(
                        String.format("Wishlist with id %s can not be found.", wishlistId)));
        checkOwnership(user, wishList);
        return wishList;
    }

    @Cacheable(value = CacheConfig.AVAILABLE_WISHLISTS_CACHE,
            key = "#ownerId + '-' + (#currentUser != null ? #currentUser.email.toLowerCase().trim() : '')")
    public List<WishListDto> getAvailableWishlists(Long ownerId, User currentUser) {
        String viewerEmail = currentUser == null ? StringUtils.EMPTY : currentUser.getEmail().trim().toLowerCase();

        return wishListRepository.findAvailableWishlists(ownerId, viewerEmail).stream()
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
