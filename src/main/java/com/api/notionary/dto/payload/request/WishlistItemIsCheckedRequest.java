package com.api.notionary.dto.payload.request;

import jakarta.validation.constraints.NotNull;
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
public class WishlistItemIsCheckedRequest {

    @NotNull(message = "isChecked field is required and cannot be null")
    private Boolean isChecked;
}