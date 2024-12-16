package com.api.notionary.repository;

import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, String> {
    List<WishList> findByUser(User user);

    @Transactional
    @Modifying
    @Query("UPDATE WishList w SET w.isPublic = :isPublic WHERE w.id = :wishlistId")
    void updateVisibility(@Param("wishlistId") String wishlistId, @Param("isPublic") Boolean isPublic);

    @Transactional
    @Modifying
    @Query("UPDATE WishList w SET w.title = :wishlistTitle WHERE w.id = :wishlistId")
    void updateTitle(@Param("wishlistId") String wishlistId, @Param("wishlistTitle") String wishlistTitle);
}
