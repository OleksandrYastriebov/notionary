package com.api.notionary.controller;

import com.api.notionary.controller.docs.ApiForbiddenErrorDoc;
import com.api.notionary.controller.docs.ApiNotFoundErrorDoc;
import com.api.notionary.controller.docs.ApiUnauthorizedErrorDoc;
import com.api.notionary.dto.ApiResponseWrapper;
import com.api.notionary.dto.user.UserProfileDto;
import com.api.notionary.entity.User;
import com.api.notionary.security.interceptor.RateLimitPlan;
import com.api.notionary.security.interceptor.RateLimited;
import com.api.notionary.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Users",
        description = "Methods for working with users"
)
@RateLimited(action = RateLimitPlan.DEFAULT)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile", description = "Returns the profile data of the currently authenticated user.")
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new UserProfileDto(user));
    }

    @Operation(summary = "Delete user by ID.",
            description = "Permanently deletes a user account and all associated data. This action cannot be undone."
    )
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
}
