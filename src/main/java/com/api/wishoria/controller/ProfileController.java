package com.api.wishoria.controller;

import com.api.wishoria.dto.user.PublicUserDto;
import com.api.wishoria.dto.user.UserAutocompleteDto;
import com.api.wishoria.dto.user.UserProfileResponseDto;
import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.UserService;
import com.api.wishoria.service.WishListService;
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

    @Operation(summary = "Autocomplete helper for searching users", description = "Returns top 5 users by email for sharing suggestions")
    @GetMapping("/autocomplete")
    public ResponseEntity<List<UserAutocompleteDto>> autocompleteUsers(@RequestParam("q") String query,
                                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.getUsersForAutocomplete(query, currentUser));
    }

    @Operation(summary = "Get user Profile", description = "Returns base info and all public wishlists.")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable Long userId,
                                                                 @AuthenticationPrincipal User currentUser) {

        PublicUserDto publicUser = userService.getPublicUserById(userId);
        List<WishListDto> availableWishlists = wishListService.getAvailableWishlists(userId, currentUser);

        return ResponseEntity.ok(new UserProfileResponseDto(publicUser, availableWishlists));
    }
}