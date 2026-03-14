package com.api.notionary.event;

import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WishlistSharedEvent extends ApplicationEvent {

    private final WishList wishlist;
    private final String targetEmail;
    private final User owner;
    boolean isTargetRegistered;

    public WishlistSharedEvent(Object source, WishList wishlist, String targetEmail, User owner, boolean isTargetRegistered) {
        super(source);
        this.wishlist = wishlist;
        this.targetEmail = targetEmail;
        this.owner = owner;
        this.isTargetRegistered = isTargetRegistered;
    }
}
