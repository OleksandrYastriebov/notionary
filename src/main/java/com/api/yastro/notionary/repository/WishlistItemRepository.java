package com.api.yastro.notionary.repository;

import com.api.yastro.notionary.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishListItem, Long> {
}
