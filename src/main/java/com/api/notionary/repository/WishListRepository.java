package com.api.notionary.repository;

import com.api.notionary.entity.User;
import com.api.notionary.entity.WishList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishList, String> {

    @EntityGraph(attributePaths = {"items"})
    List<WishList> findByUser(User user);
}
