package com.api.notionary.dto.wishlist;

import com.api.notionary.dto.wishlistitem.WishListItemDto;
import com.api.notionary.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WishListDto {

    private String id;
    @JsonIgnore
    @ToString.Exclude
    private User user;
    private List<WishListItemDto> wishListItems = new ArrayList<>();
    private String title;
    private Boolean isPublic = false;
    private LocalDateTime createdAt;

}
