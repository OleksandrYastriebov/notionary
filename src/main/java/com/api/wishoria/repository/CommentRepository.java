package com.api.wishoria.repository;

import com.api.wishoria.entity.Comment;
import com.api.wishoria.entity.WishListItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author"})
    List<Comment> findByWishListItemOrderByCreatedAtAsc(WishListItem wishListItem);

    List<Comment> findByAuthorId(String authorId);
}
