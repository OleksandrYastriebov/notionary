package com.api.notionary.dto.payload.request.wishlistitem;

import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import jakarta.validation.constraints.DecimalMin;
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

    @Size(min = 1, message = "Title can not be empty")
    private String title;

    @Size(min = 1, message = "Url can not be empty")
    private String url;

    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Size(min = 1, message = "Description can not be empty")
    private String description;

    public WishListItem toEntity(WishList wishList) {
        return new WishListItem(
                wishList,
                title,
                url,
                description,
                price
        );
    }

}
