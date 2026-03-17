package com.api.wishoria.controller;

import com.api.wishoria.controller.docs.ApiForbiddenErrorDoc;
import com.api.wishoria.controller.docs.ApiNotFoundErrorDoc;
import com.api.wishoria.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.wishoria.dto.ApiResponseWrapper;
import com.api.wishoria.dto.payload.request.user.ChangePasswordRequest;
import com.api.wishoria.dto.payload.request.user.UpdateUserRequest;
import com.api.wishoria.dto.user.UserProfileDto;
import com.api.wishoria.entity.User;
import com.api.wishoria.security.interceptor.RateLimitPlan;
import com.api.wishoria.security.interceptor.RateLimited;
import com.api.wishoria.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Methods for working with users")
@RateLimited(action = RateLimitPlan.DEFAULT)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile", description = "Returns the profile data of the currently authenticated user.")
    @ApiUnauthorizedErrorDoc
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserProfileDto(user));
    }

    @Operation(summary = "Delete user by ID.",
            description = "Permanently deletes a user account and all associated data. This action cannot be undone.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User with id 1 was successfully removed.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))})
    @ApiNotFoundErrorDoc
    @ApiUnauthorizedErrorDoc
    @ApiForbiddenErrorDoc
    @RateLimited(action = RateLimitPlan.AUTH)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper> deleteUser(@PathVariable
                                                         @Parameter(description = "Unique identifier of the user", example = "10")
                                                         Long id,
                                                         @AuthenticationPrincipal User currentUser) {
        userService.deleteUserById(id, currentUser);
        return ResponseEntity.ok(new ApiResponseWrapper(String.format("User with id %s was successfully removed.", id)));
    }

    @Operation(summary = "Update current user profile", description = "Updates the first name, last name, or avatar URL of the authenticated user.")
    @ApiUnauthorizedErrorDoc
    @PatchMapping("/me")
    @RateLimited(action = RateLimitPlan.MUTATION)
    public ResponseEntity<UserProfileDto> updateUserProfile(@AuthenticationPrincipal User user,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(user, request));
    }

    @Operation(summary = "Change user password", description = "Updates the authenticated user's password after verifying the current password.")
    @ApiUnauthorizedErrorDoc
    @PatchMapping("/me/password")
    @RateLimited(action = RateLimitPlan.MUTATION)
    public ResponseEntity<ApiResponseWrapper> changePassword(@AuthenticationPrincipal User user,
                                                             @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user, request);
        return ResponseEntity.ok(new ApiResponseWrapper("Password updated successfully."));
    }
}
