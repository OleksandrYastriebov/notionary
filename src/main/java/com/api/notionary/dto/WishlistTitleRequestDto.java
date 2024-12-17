package com.api.notionary.dto;

import jakarta.validation.constraints.NotBlank;
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
public class WishlistTitleRequestDto {

    @NotNull(message = "Field title is required and cannot be null")
    @NotBlank(message = "Field title is required and cannot be empty")
    private String title;

}
