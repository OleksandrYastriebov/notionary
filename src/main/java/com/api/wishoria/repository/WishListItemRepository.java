package com.api.wishoria.repository;

import com.api.wishoria.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishListItemRepository extends JpaRepository<WishListItem, String> {

    Optional<WishListItem> findByIdAndWishListId(String wishlistItemId, String wishlistId);

    int countByWishListId(String wishListId);

}
