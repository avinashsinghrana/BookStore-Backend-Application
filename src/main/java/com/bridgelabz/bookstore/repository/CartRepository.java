package com.bridgelabz.bookstore.repository;

import java.awt.print.Book;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bridgelabz.bookstore.model.CartModel;

@Repository
@Transactional
public interface CartRepository extends JpaRepository<CartModel, Long> {

	@Query(value = "select c from CartModel c where c.bookId=:book_id")
	Optional<CartModel> findByBookId(@Param("book_id") Long book_id);

	@Query(value = "select * from cart where user_id=:userId", nativeQuery = true)
	List<CartModel> findByUserId(@Param("userId") Long userId);

	@Query(value = "select * from cart where book_id=:book_id and user_id=:user_id", nativeQuery = true)
	Optional<CartModel> findByBookIdAndUserId(@Param("book_id") Long book_id,@Param("user_id") Long user_id);

    boolean existsByBookIdAndUserId(Long bookId, long id);


    //@Query(value="delete * from Cart where book_id=?" ,nativeQuery = true)
	//Optional<CartModel> removeAllItem(Long bookId);

	//CartModel findByBookId(Long book_id);
}
