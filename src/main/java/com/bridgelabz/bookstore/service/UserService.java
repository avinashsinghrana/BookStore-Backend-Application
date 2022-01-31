package com.bridgelabz.bookstore.service;

import com.bridgelabz.bookstore.dto.*;
import com.bridgelabz.bookstore.exception.BookException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.CartModel;
import com.bridgelabz.bookstore.response.UserAddressDetailsResponse;
import org.springframework.stereotype.Component;

import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.response.UserDetailsResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Component
public interface UserService {

    boolean register(RegistrationDto registrationDto) throws UserException;

    boolean verify(String token);

	UserDetailsResponse forgetPassword(ForgotPasswordDto emailId);

    boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException;

    Response login(LoginDto logindto) throws UserException;

	//Response addToCart(Long bookId) throws BookException;
    Response addToCart(CartDto cartDto,Long bookId,String token);

    Response addMoreItems(Long bookId) throws BookException;

    Response removeItem(Long bookId) throws BookException;

    Response removeByBookId(Long bookId,String token) throws BookException;

    Response removeAll(String token);

   // List<CartModel> getAllItemFromCart() throws BookException;
    List<CartModel> getAllItemFromCart(String token) throws BookException;

    List<BookModel> sortBookByAsc();

	List<BookModel> sortBookByDesc();

	List<BookModel> getAllBooks() throws UserException;

	BookModel getBookDetails(Long bookId) throws UserException;

    /// to get user details to place order
    UserAddressDetailsResponse getUserDetails(String token);

    // add new user details
    Response addUserDetails(UserDetailsDTO userDetail,String locationType, long userId);

    // update existing user details
    Response deleteUserDetails(UserDetailsDTO userDetail, long userId);
     List<BookModel> getAllVerifiedBooks() throws UserException;
    
    Long getIdFromToken(String token);
    String uploadFile(MultipartFile file, String token);
    
    Optional<BookModel> searchBookByName(String bookName);
    Optional<BookModel> searchBookByAuthor(String authorName);

    long getOrderId();

	Response addItems(Long bookId, int quantity) throws BookException;
	
	Response orderPlaced(String token) throws BookException;

    Response addToWishList(Long bookId, String token);

    Response deleteFromWishlist(Long bookId, String token);

    Response addFromWishlistToCart(Long bookId, String token);

    Response getAllItemFromWishList(String token);
}
