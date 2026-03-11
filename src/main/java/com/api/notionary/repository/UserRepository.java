package com.api.notionary.repository;

import com.api.notionary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.id IN :ids")
    void bulkDeleteByIds(@Param("ids") List<Long> ids);


    /**
     * Finds all users with "enabled = false" property and expired confirmation token;
     * */
    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.enabled = false
        AND u.id IN (
            SELECT ct.user.id
            FROM ConfirmationToken ct
            WHERE ct.expiresAt < CURRENT_TIMESTAMP
        )
    """)
    List<Long> findIdsOfExpiredAndDisabledUsers();

}
