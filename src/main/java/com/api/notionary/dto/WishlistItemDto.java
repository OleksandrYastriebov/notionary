package com.api.notionary.dto;

import com.api.notionary.entity.WishList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class WishlistItemDto {

    private String id;
    @JsonIgnore
    private WishList wishList;
    private String title;
    private String url;
    private Double price;
    private String description;
    private Boolean isChecked;
}
