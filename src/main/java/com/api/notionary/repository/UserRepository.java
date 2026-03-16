package com.api.notionary.repository;

import com.api.notionary.entity.User;
import org.springframework.data.domain.Pageable;
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
     *
     */
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


    @Query("""
            SELECT u FROM User u WHERE u.id != :currentUserId AND (
                        LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR
                        LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                        LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))""")
    List<User> searchUsersByQuery(@Param("query") String query, @Param("currentUserId") Long currentUserId, Pageable pageable);
}
