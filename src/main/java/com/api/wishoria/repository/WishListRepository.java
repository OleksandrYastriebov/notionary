package com.api.wishoria.repository;

import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, String> {

    @EntityGraph(attributePaths = {"items"})
    List<WishList> findByUser(User user);

    int countByUser(User user);

    List<WishList> findAllByIsPublicTrue();

    @Query("SELECT w FROM WishList w WHERE w.user.id = :ownerId AND " +
            "(w.isPublic = true OR w IN " +
            "(SELECT wa.wishList FROM WishlistAccess wa WHERE LOWER(wa.grantedUserEmail) = LOWER(:viewerEmail)))")
    List<WishList> findAvailableWishlists(@Param("ownerId") Long ownerId, @Param("viewerEmail") String viewerEmail);
}
