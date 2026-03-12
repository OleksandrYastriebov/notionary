package com.api.notionary.dto.payload.request.wishlist;

import com.api.notionary.entity.WishList;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWishlistRequest {

    @Size(min = 1, max = 100, message = "Title is too long. Max 100 characters.")
    private String title;

    private Boolean isPublic;

    @Size(max = 2048, message = "Image URL is too long")
    @URL(message = "Invalid URL format")
    private String imageUrl;

    public void updateEntity(WishList existingWishlist) {
        if (this.title != null) existingWishlist.setTitle(this.title);
        if (this.isPublic != null) existingWishlist.setIsPublic(this.isPublic);
        if (this.imageUrl != null) existingWishlist.setImageUrl(this.imageUrl);
    }

}
