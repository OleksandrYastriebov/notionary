package com.api.notionary.dto;

import com.api.notionary.entity.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class WishlistDto {

    private String id;
    private User user;
    private String title;
    private boolean isPublic;
    private LocalDateTime createdAt;

}
