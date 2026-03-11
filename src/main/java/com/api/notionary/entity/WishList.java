package com.api.notionary.entity;

import com.api.notionary.dto.wishlist.WishListDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
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
@Table(name = "wishlist")
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "wishList", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<WishListItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "wishList", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<WishlistAccess> accesses = new ArrayList<>();

    public WishList(User user, String title, Boolean isPublic) {
        this.user = user;
        this.title = title;
        this.isPublic = isPublic;
    }

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID()
                .toString()
                .replace("-", "");
        this.createdAt = LocalDateTime.now();
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
                user,
                items.stream()
                        .map(WishListItem::toDto)
                        .toList(),
                title,
                isPublic,
                createdAt
        );
    }
}
