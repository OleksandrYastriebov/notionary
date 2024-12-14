package com.api.notionary.repository;

import com.api.notionary.entity.WishlistAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistAccessRepository extends JpaRepository<WishlistAccess, Long> {
}
