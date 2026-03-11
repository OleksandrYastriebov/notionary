package com.api.notionary.repository;

import com.api.notionary.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListItemRepository extends JpaRepository<WishListItem, String> {

    Optional<WishListItem> findByIdAndWishListId(String wishlistItemId, String wishlistId);

}
