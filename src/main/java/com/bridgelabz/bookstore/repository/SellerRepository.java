package com.bridgelabz.bookstore.repository;

import com.bridgelabz.bookstore.model.BookModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bridgelabz.bookstore.model.SellerModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<SellerModel,Long> {
    @Query(value = "SELECT * from  sellerbooks where seller_id =?", nativeQuery = true)
    List<BookModel> getSellerBooks(long seller_id);

    @Query(value = "SELECT * from  seller where email_id =:email_id", nativeQuery = true)
    Optional<SellerModel> getSellerByEmailId(@Param("email_id") String email_id);

    @Query(value = "SELECT seller_id from seller where user_id =:user_id", nativeQuery = true)
    long getSellerId(@Param("user_id") long user_id);

    @Query(value = "SELECT * from  seller where user_id =:user_id", nativeQuery = true)
    Optional<SellerModel> getSeller(@Param("user_id") long user_id);
	
}
