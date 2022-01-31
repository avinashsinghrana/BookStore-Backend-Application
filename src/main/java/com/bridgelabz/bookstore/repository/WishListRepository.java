package com.bridgelabz.bookstore.repository;

import com.bridgelabz.bookstore.model.WishListModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishListRepository extends JpaRepository<WishListModel, Long> {
    boolean existsByBookIdAndUserId(Long bookId, long id);

    WishListModel findByBookIdAndUserId(Long bookId, long id);

    List<WishListModel> findAllByUserId(long id);
}
