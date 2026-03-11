package com.api.notionary.dto.payload.request.wishlistitem;

import com.api.notionary.entity.WishListItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWishListItemRequest {

    @Size(min = 1, max = 100, message = "Title should be between 1 and 100 characters")
    private String title;

    @Size(max = 2048, message = "URL is too long")
    @URL(message = "Invalid URL format")
    private String url;

    @DecimalMin(value = "0.0", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid (e.g. 12345678.99)")
    private BigDecimal price;

    @Size(max = 1000, message = "Description should not more than 1000 characters")
    private String description;

    private Boolean isChecked;

    public void updateEntity(WishListItem existingItem) {
        if (this.title != null) existingItem.setTitle(this.title);
        if (this.url != null) existingItem.setUrl(this.url);
        if (this.price != null) existingItem.setPrice(this.price);
        if (this.description != null) existingItem.setDescription(this.description);
        if (this.isChecked != null) existingItem.setChecked(this.isChecked);
    }
}
