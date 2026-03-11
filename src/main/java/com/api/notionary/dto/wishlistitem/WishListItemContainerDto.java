package com.api.notionary.dto.wishlistitem;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WishListItemContainerDto {


    private List<WishListItemDto> wishListItemDtos;

}
