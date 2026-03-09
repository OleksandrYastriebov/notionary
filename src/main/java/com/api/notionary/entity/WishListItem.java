package com.api.notionary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "wishlist_item")
public class WishListItem {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    @JsonIgnore
    private WishList wishList;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "description")
    private String description;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked;

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
        return 31;
    }
}
