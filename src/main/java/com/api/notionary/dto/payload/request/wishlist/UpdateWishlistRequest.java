package com.api.notionary.dto.payload.request.wishlist;

import com.api.notionary.entity.WishList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWishlistRequest {

    @Size(min = 1, max = 100, message = "Title is too long. Max 100 characters.")
    private String title;
    private Boolean isPublic;

    public void updateEntity(WishList existingWishlist) {
        if (this.title != null) {
            existingWishlist.setTitle(this.title);
        }
        if (this.isPublic != null) {
            existingWishlist.setIsPublic(this.isPublic);
        }
    }

}
