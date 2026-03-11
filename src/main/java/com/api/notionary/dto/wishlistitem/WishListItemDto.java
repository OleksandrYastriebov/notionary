package com.api.notionary.dto.wishlistitem;

import com.api.notionary.entity.WishList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WishListItemDto {

    private String id;
    @JsonIgnore
    @ToString.Exclude
    private WishList wishList;
    private String title;
    private String url;
    private BigDecimal price;
    private String description;
    private Boolean isChecked;

}
