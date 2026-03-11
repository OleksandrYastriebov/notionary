package com.api.notionary.dto.payload.request.wishlistitem;

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
public class UpdateWishListItemRequest {

    @Size(min = 1, message = "Title can not be empty")
    private String title;

    @Size(min = 1, message = "Url can not be empty")
    private String url;

    @DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Size(min = 1, message = "Description can not be empty")
    private String description;

    private Boolean isChecked;

    public void updateEntity(WishListItem existingItem) {
        if (this.title != null) existingItem.setTitle(this.title);
        if (this.url != null) existingItem.setUrl(this.url);
        if (this.price != null) existingItem.setPrice(this.price);
        if (this.description != null) existingItem.setDescription(this.description);
        if (this.isChecked != null) existingItem.setIsChecked(this.isChecked);
    }
}
