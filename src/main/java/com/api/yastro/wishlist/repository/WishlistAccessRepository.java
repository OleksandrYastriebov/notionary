package com.api.yastro.wishlist.repository;

import com.api.yastro.wishlist.entity.WishlistAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistAccessRepository extends JpaRepository<WishlistAccess, Long> {
}
