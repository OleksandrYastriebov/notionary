package com.api.notionary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "wishlist_item")
public class WishListItem {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "wishlist_id", nullable = false)
    @JsonIgnore
    private WishList wishList;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "price")
    private Double price;

    @Column(name = "description")
    private String description;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked;

    @PrePersist
    protected void onCreate() {
        this.isChecked = false;
        this.id = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 15);
    }
}
