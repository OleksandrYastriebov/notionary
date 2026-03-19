package com.api.wishoria.entity;

import com.api.wishoria.dto.wishlist.WishListDto;
import com.api.wishoria.util.IdGenerationUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wishlist", indexes = {
        @Index(name = "idx_wishlist_user_id", columnList = "user_id"),
        @Index(name = "idx_wishlist_created_at", columnList = "created_at")
})
public class WishList {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "wishList", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<WishListItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "wishList", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<WishlistAccess> accesses = new ArrayList<>();

    public WishList(User user, String title, Boolean isPublic, String imageUrl) {
        this.user = user;
        this.title = title;
        this.isPublic = isPublic;
        this.imageUrl = imageUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.id = IdGenerationUtil.generateNanoId();
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishList wishList)) return false;
        if (id == null || wishList.getId() == null) return false;

        return Objects.equals(id, wishList.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public WishListDto toDto() {
        return new WishListDto(
                id,
                user.getId(),
                items.stream()
                        .map(WishListItem::toDto)
                        .toList(),
                title,
                isPublic,
                imageUrl,
                createdAt
        );
    }
}
