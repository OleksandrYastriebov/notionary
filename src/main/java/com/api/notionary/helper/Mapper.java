package com.api.notionary.helper;

import com.api.notionary.dto.UserDto;
import com.api.notionary.dto.WishlistDto;
import com.api.notionary.dto.WishlistItemDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishListItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    private ModelMapper modelMapper = new ModelMapper();

    /* Wishlist mappings */

    public WishlistDto mapWishlistToDto(WishList wishList) {
        return modelMapper.map(wishList, WishlistDto.class);
    }

    public WishList mapWishlistToEntity(WishlistDto wishlistDto) {
        return modelMapper.map(wishlistDto, WishList.class);
    }

    /* Wishlist Item mappings */

    public WishlistItemDto mapWishlistItemToDto(WishListItem wishListItem) {
        return modelMapper.map(wishListItem, WishlistItemDto.class);
    }

    public WishListItem mapWishlistItemToEntity(WishlistItemDto wishlistItemDto) {
        return modelMapper.map(wishlistItemDto, WishListItem.class);
    }

    /* User mappings */

    public UserDto mapUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public User mapUserToEntity(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
