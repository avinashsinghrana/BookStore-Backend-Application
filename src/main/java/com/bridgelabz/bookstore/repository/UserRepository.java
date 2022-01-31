package com.bridgelabz.bookstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.bridgelabz.bookstore.model.UserModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

	UserModel findByUserId(long userId);

	UserModel findByEmailId(String emailId);
	
	@Query(value="Select * from user where email_id = :emailId",nativeQuery = true)
	UserModel findEmail(String emailId);
	
	@Query(value = "select role_type from user where user_id = ?", nativeQuery = true)
	String checkRole(long userId);

	@Query("select u from UserModel u where u.emailId=:emailId")
	Optional<UserModel> getByEmailId(@Param("emailId") String emailId);
}
