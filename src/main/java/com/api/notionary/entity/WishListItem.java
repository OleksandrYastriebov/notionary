package com.api.notionary.entity;

import com.api.notionary.dto.wishlistitem.WishListItemDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wishlist_item")
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

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked = false;

    public WishListItem(WishList wishList, String title, String url,
                        String description, BigDecimal price) {
        this.wishList = wishList;
        this.title = title;
        this.description = description;
        this.url = url;
        this.price = price;
    }

    @PrePersist
    protected void onCreate() {
        this.isChecked = false;
        this.id = UUID.randomUUID()
                .toString()
                .replace("-", "");
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
                isChecked
        );
    }
}
