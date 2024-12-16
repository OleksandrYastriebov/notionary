package com.api.notionary.repository;

import com.api.notionary.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishListItem, String> {

    Optional<WishListItem> findByIdAndWishListId(String wishlistItemId, String wishlistId);
    List<WishListItem> findAllByWishListId(String wishlistId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WishListItem w WHERE w.id = :wishlistItemId AND w.wishList.id = :wishlistId")
    void deleteByIdAndWishListId(@Param("wishlistItemId") String wishlistItemId, @Param("wishlistId") String wishlistId);


}
