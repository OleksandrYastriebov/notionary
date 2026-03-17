package com.api.wishoria.service;

import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.access.AccessesContainerDto;
import com.api.wishoria.dto.payload.request.wishlist.RevokeAccessRequest;
import com.api.wishoria.dto.payload.request.wishlist.ShareWishListRequest;
import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import com.api.wishoria.entity.WishlistAccess;
import com.api.wishoria.event.WishlistSharedEvent;
import com.api.wishoria.exception.EntityNotFoundException;
import com.api.wishoria.repository.UserRepository;
import com.api.wishoria.repository.WishListAccessRepository;
import com.api.wishoria.repository.WishListRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WishListAccessService {

    @Value("${app.limits.max-shares-per-wishlist}")
    private int maxShares;

    private final ApplicationEventPublisher eventPublisher;
    private final WishListAccessRepository wishlistAccessRepository;
    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;

    public AccessesContainerDto getAllGrantedEmailsForWishlist(String wishlistId, User user) {
        getWishlistAndVerifyOwner(wishlistId, user);
        return new AccessesContainerDto(wishlistAccessRepository.findEmailsByWishlistId(wishlistId));
    }

    @Transactional
    public ApiResponseWrapper grantAccess(String wishlistId, ShareWishListRequest request, User user) {
        WishList wishlist = getWishlistAndVerifyOwner(wishlistId, user);
        String targetEmail = request.email().toLowerCase().trim();

        if (user.getEmail().equalsIgnoreCase(targetEmail)) {
            throw new IllegalStateException("You cannot share a wishlist with yourself");
        }

        if (wishlistAccessRepository.existsByWishListAndGrantedUserEmail(wishlist, targetEmail)) {
            throw new IllegalStateException("User already has access to this wishlist");
        }
        verifyMaxShareCount(wishlistId);

        sendEmailNotification(user, wishlist, targetEmail);
        wishlistAccessRepository.save(new WishlistAccess(wishlist, targetEmail));

        return new ApiResponseWrapper("Access granted successfully to " + targetEmail);
    }

    @Transactional
    public ApiResponseWrapper revokeAccess(String wishlistId, RevokeAccessRequest request, User user) {
        WishList wishlist = getWishlistAndVerifyOwner(wishlistId, user);

        WishlistAccess access = wishlistAccessRepository
                .findByWishListAndGrantedUserEmail(wishlist, request.email().toLowerCase().trim())
                .orElseThrow(() -> new EntityNotFoundException("Access record not found for this email"));

        wishlistAccessRepository.delete(access);

        return new ApiResponseWrapper("Access revoked successfully for " + request.email());
    }

    private WishList getWishlistAndVerifyOwner(String wishlistId, User owner) {
        if (owner == null) {
            throw new AccessDeniedException("Authentication is required to perform this action");
        }

        WishList wishlist = wishListRepository.findById(wishlistId)
                .orElseThrow(() -> new EntityNotFoundException("Wishlist not found"));

        if (!wishlist.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException("Only the owner can manage access to this wishlist");
        }
        return wishlist;
    }


    private void verifyMaxShareCount(String wishlistId) {
        if (wishlistAccessRepository.countByWishListId(wishlistId) >= maxShares) {
            throw new IllegalStateException("Maximum limit of shared users reached for this wishlist.");
        }
    }

    private void sendEmailNotification(User user, WishList wishlist, String targetEmail) {
        eventPublisher.publishEvent(
                new WishlistSharedEvent(this, wishlist, targetEmail, user, userRepository.existsByEmail(targetEmail)));
    }
}