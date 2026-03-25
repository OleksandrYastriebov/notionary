package com.api.wishoria.repository;

import com.api.wishoria.entity.User;
import com.api.wishoria.entity.WishList;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<WishList, String> {

    @EntityGraph(attributePaths = {"items"})
    Page<WishList> findByUser(User user, Pageable pageable);

    int countByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WishList w WHERE w.id = :id")
    Optional<WishList> findByIdWithLock(@Param("id") String id);

    List<WishList> findAllByIsPublicTrue();

    @Query("""
            SELECT w FROM WishList w
            WHERE w.user.id = :ownerId
              AND (
                  w.isPublic = true
                  OR EXISTS (
                      SELECT 1 FROM WishlistAccess wa
                      WHERE wa.wishList = w
                        AND LOWER(wa.grantedUserEmail) = LOWER(:viewerEmail)
                  )
              )
            """)
    Page<WishList> findAvailableWishlists(@Param("ownerId") Long ownerId, @Param("viewerEmail") String viewerEmail, Pageable pageable);
}
