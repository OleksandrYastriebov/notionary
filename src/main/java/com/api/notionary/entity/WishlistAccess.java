package com.api.notionary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "wishlist_access")
public class WishlistAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    private WishList wishList;

    @Column(name = "granted_user_email", nullable = false, length = 80)
    private String grantedUserEmail;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistAccess that)) return false;
        return Objects.equals(grantedUserEmail, that.getGrantedUserEmail()) &&
                Objects.equals(
                        wishList != null ? wishList.getId() : null,
                        that.getWishList() != null ? that.getWishList().getId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                grantedUserEmail,
                wishList != null ? wishList.getId() : null
        );
    }
}
