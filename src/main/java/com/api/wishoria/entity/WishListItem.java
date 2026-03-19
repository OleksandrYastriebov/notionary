package com.api.wishoria.entity;

import com.api.wishoria.dto.wishlistitem.WishListItemDto;
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

import java.math.BigDecimal;
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
@Table(name = "wishlist_item", indexes = {
        @Index(name = "idx_wishlist_item_wishlist_id", columnList = "wishlist_id")
})
public class WishListItem {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    @ToString.Exclude
    private WishList wishList;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "url", length = 2048)
    private String url;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_by_user_id")
    @ToString.Exclude
    private User checkedBy;

    @OneToMany(mappedBy = "wishListItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    public WishListItem(WishList wishList, String title, String url,
                        String description, BigDecimal price, String imageUrl) {
        this.wishList = wishList;
        this.title = title;
        this.description = description;
        this.url = url;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    @PrePersist
    protected void onCreate() {
        this.isChecked = false;
        this.id = IdGenerationUtil.generateNanoId();
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishListItem that)) return false;
        if (id == null || that.getId() == null) return false;

        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public WishListItemDto toDto() {
        return new WishListItemDto(
                id,
                wishList.getId(),
                title,
                url,
                price,
                description,
                imageUrl,
                isChecked,
                checkedBy == null ? null : checkedBy.getId(),
                createdAt
        );
    }
}
