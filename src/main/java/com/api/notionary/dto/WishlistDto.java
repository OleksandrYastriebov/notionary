package com.api.notionary.dto;

import com.api.notionary.entity.User;
import com.api.notionary.entity.WishListItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class WishlistDto {

    private String id;
    @JsonIgnore
    private User user;
    private List<WishlistItemDto> wishListItems = List.of();
    private String title;
    private Boolean isPublic;
    private LocalDateTime createdAt;

}
