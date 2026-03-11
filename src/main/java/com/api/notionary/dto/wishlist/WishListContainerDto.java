package com.api.notionary.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WishListContainerDto {

    private final List<WishListDto> wishListList;

}
