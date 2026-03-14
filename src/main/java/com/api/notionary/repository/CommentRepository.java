package com.api.notionary.repository;

import com.api.notionary.entity.Comment;
import com.api.notionary.entity.WishListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByWishListItemOrderByCreatedAtAsc(WishListItem wishListItem);

    List<Comment> findByAuthorId(String authorId);

    void deleteByWishListItem(WishListItem wishListItem);
}
