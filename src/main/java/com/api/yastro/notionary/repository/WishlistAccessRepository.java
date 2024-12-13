package com.api.yastro.notionary.repository;

import com.api.yastro.notionary.entity.WishlistAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistAccessRepository extends JpaRepository<WishlistAccess, Long> {
}
