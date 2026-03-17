package com.api.wishoria.repository;

import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, String> {

    @EntityGraph(attributePaths = {"items"})
    List<WishList> findByUser(User user);

    int countByUser(User user);

    /**
     * Finds all wishlists for user where isPublic is true
     */
    List<WishList> findByUserIdAndIsPublicTrue(Long userId);

    List<WishList> findAllByIsPublicTrue();

}
