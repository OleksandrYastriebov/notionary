package com.api.notionary.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "wishlist_access", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"wishlist_id", "granted_user_email"})
})
public class WishlistAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    @ToString.Exclude
    private WishList wishList;

    @Column(name = "granted_user_email", nullable = false, length = 80)
    private String grantedUserEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public WishlistAccess(WishList wishList, String grantedUserEmail) {
        this.wishList = wishList;
        this.grantedUserEmail = grantedUserEmail;
    }

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
