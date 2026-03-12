package com.api.notionary.dto.payload.request.wishlistitem;

import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateWishListItemRequest {

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 2048, message = "URL is too long")
    @URL(message = "Invalid URL format")
    private String url;

    @DecimalMin(value = "0.0", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid (e.g. 12345678.99)")
    private BigDecimal price;

    @Size(max = 1000, message = "Description should not more than 1000 characters")
    private String description;

    @Size(max = 2048, message = "Image URL is too long")
    @URL(message = "Invalid URL format")
    private String imageUrl;

    public WishListItem toEntity(WishList wishList) {
        return new WishListItem(
                wishList,
                title,
                url,
                description,
                price,
                imageUrl
        );
    }

}
