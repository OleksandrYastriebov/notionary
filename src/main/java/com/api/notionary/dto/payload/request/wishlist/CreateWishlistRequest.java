package com.api.notionary.dto.payload.request.wishlist;

import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateWishlistRequest {

    @NotBlank(message = "Title can not be empty")
    @Size(max = 100, message = "Title is too long. Max 100 characters.")
    private String title;
    private Boolean isPublic;

    public WishList toEntity(User user) {
        return new WishList(
                user,
                title,
                isPublic != null ? isPublic : false
        );
    }
}
