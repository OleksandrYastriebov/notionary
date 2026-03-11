package com.api.notionary.dto.payload.request.wishlistitem;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WishlistItemIsCheckedRequest {

    @NotNull(message = "isChecked flag cannot be null")
    private Boolean isChecked;

}