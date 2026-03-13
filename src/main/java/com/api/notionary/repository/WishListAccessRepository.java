package com.api.notionary.repository;

import com.api.notionary.entity.WishList;
import com.api.notionary.entity.WishlistAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListAccessRepository extends JpaRepository<WishlistAccess, Long> {

    boolean existsByWishListAndGrantedUserEmail(WishList wishList, String email);

    Optional<WishlistAccess> findByWishListAndGrantedUserEmail(WishList wishList, String email);

    @Query("SELECT wa.grantedUserEmail FROM WishlistAccess wa WHERE wa.wishList.id = :wishlistId")
    List<String> findEmailsByWishlistId(@Param("wishlistId") String wishlistId);

    void deleteByWishList(WishList wishList);

}
