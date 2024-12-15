package com.api.notionary.service;

import com.api.notionary.dto.WishlistDto;
import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import com.api.notionary.exception.WishlistNotFoundException;
import com.api.notionary.helper.Mapper;
import com.api.notionary.repository.UserRepository;
import com.api.notionary.repository.WishListRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final Mapper mapper;

    @Autowired
    public WishlistService(WishListRepository wishListRepository, UserRepository userRepository, Mapper mapper) {
        this.wishListRepository = wishListRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public WishlistDto findWishlistById(String id) {
        WishList wishlist = wishListRepository
                .findById(id)
                .orElseThrow(() -> new WishlistNotFoundException(String.format("Wishlist with id %s can not be found.", id)));
        return mapper.mapWishlistToDto(wishlist);
    }

    @Transactional
    public WishlistDto saveWishlist(WishlistDto wishlistDto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("User not found in repository"));
        wishlistDto.setUser(user);
        WishList wishlist = mapper.mapWishlistToEntity(wishlistDto);
        return mapper.mapWishlistToDto(wishListRepository.save(wishlist));
    }

    public List<WishlistDto> getWishlistsForUser(User user) {
        List<WishList> wisList = wishListRepository.findByUser(user);
        return wisList.stream().map(item -> {
            WishlistDto wishlistDto = new WishlistDto();
            wishlistDto.setId(item.getId());
            wishlistDto.setPublic(item.getIsPublic());
            wishlistDto.setTitle(item.getTitle());
            wishlistDto.setCreatedAt(item.getCreatedAt());
            return wishlistDto;
        }).toList();
    }

    public boolean isWishlistOwner(String id, String userEmail) {
        WishlistDto wishlistDto = findWishlistById(id);
        return userEmail.equals(wishlistDto.getUser().getEmail());
    }

    @Transactional
    public void deleteWishList(String id) {
        if (findWishlistById(id) == null) {
            throw new WishlistNotFoundException(
                    (String.format("*ERROR* Trying to delete wishlist with id %s. Wishlist not found", id)));
        }
        wishListRepository.deleteById(id);
    }

}
