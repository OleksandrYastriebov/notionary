package com.api.yastro.wishlist.repository;

import com.api.yastro.wishlist.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishListItem, Long> {
}
