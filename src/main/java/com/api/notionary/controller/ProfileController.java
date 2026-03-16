package com.api.notionary.controller;

import com.api.notionary.dto.user.PublicUserDto;
import com.api.notionary.dto.user.UserProfileResponseDto;
import com.api.notionary.dto.wishlist.WishListDto;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.UserService;
import com.api.notionary.service.WishListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Social Profile", description = "Users search anv public profiles views")
@RateLimited(action = RateLimitPlan.DEFAULT)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final UserService userService;
    private final WishListService wishListService;

    @Operation(summary = "Get User List", description = "Finds users by email, first name or last name")
    @GetMapping("/search")
    public ResponseEntity<List<PublicUserDto>> searchUsers(@RequestParam("q") String query,
                                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.searchPublicUsers(query, currentUser));
    }

    @Operation(summary = "Get user Profile", description = "Returns base info and all public wishlists.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable Long userId) {

        PublicUserDto publicUser = userService.getPublicUserById(userId);
        List<WishListDto> publicWishlists = wishListService.getPublicWishlistsForUser(userId);

        return ResponseEntity.ok(new UserProfileResponseDto(publicUser, publicWishlists));
    }
}