package com.api.notionary.dto.wishlistitem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WishListItemDto {

    private String id;
    private String wishListId;
    private String title;
    private String url;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Boolean isChecked;

}
