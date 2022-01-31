package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.bridgelabz.bookstore.enums.RoleType;
import com.bridgelabz.bookstore.model.SellerModel;
import com.bridgelabz.bookstore.utility.CommonUtility;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.SellerRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.ElasticSearchService;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.utility.JwtGenerator;

@Service
public class SellerServiceImplementation implements SellerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private Environment environment;

    @Autowired
    JwtGenerator jwtop;


    @Override

    public Response addBook(BookDto newBook, String token) throws UserException {
        UserModel user = CommonUtility.validateUser(token);
        if (RoleType.SELLER.equals(user.getRoleType())) {
            BookModel book = new BookModel();
            BeanUtils.copyProperties(newBook, book);
            book.setBookImgUrl(newBook.getBookImgUrl());
            book.setIsDisApproved(false);
            book.setIsSendForApproval(false);
            book.setUser(user);
            BookModel books = bookRepository.save(book);
            user.getBooks().add(books);
            userRepository.save(user);
            return new Response(environment.getProperty("book.verification.status"), HttpStatus.OK.value(), book);
        } else {
            throw new UserException(environment.getProperty("book.unauthorised.status"), HttpStatus.FORBIDDEN.value());
        }

    }

    @Override
    public Response updateBook(UpdateBookDto newBook, String token, Long bookId) throws UserException {
        UserModel user = CommonUtility.validateUser(token);
        if (RoleType.SELLER.equals(user.getRoleType())) {
            Optional<BookModel> book = bookRepository.findById(bookId);
            if (book.isPresent()) {
                if (!isVerificationRequired(newBook, book.get())) {
                    book.get().setQuantity(newBook.getQuantity());
                } else {
                    book.get().setIsSendForApproval(false);
                    book.get().setIsDisApproved(false);
                    book.get().setVerfied(false);
                }
                BeanUtils.copyProperties(newBook, book.get());
                book.get().setUpdatedDateAndTime(LocalDateTime.now());
                bookRepository.save(book.get());
                return new Response(HttpStatus.OK.value(), "Book update Successfully Need to Verify");
            } else
                return new Response(HttpStatus.OK.value(), "Invalid book details, please check request");
        }
        return new Response(HttpStatus.OK.value(), "Book Not updated Becoz Not Authoriized to add Book");
    }

    private boolean isVerificationRequired(UpdateBookDto newBook, BookModel book) {
        boolean isVerificationRequired = false;
        if (!book.isVerfied())
            return true;
        isVerificationRequired = newBook.getQuantity() != book.getQuantity();
        isVerificationRequired = Objects.equals(newBook.getBookName(), book.getBookName());
        isVerificationRequired = Objects.equals(newBook.getAuthorName(), book.getAuthorName());
        isVerificationRequired = Objects.equals(newBook.getPrice(), book.getPrice());
        isVerificationRequired = Objects.equals(newBook.getBookDetails(), book.getBookDetails());
        return !isVerificationRequired;
    }

    @Override
    public Response deleteBook(String token, Long bookId) {
        UserModel userModel = CommonUtility.validateUser(token);
        if (RoleType.SELLER.equals(userModel.getRoleType()) || RoleType.ADMIN.equals(userModel.getRoleType())) {
            bookRepository.deleteById(bookId);
            return new Response(HttpStatus.OK.value(), "Book deleted Successfully ");
        }
        return new Response(HttpStatus.OK.value(), "Book Not deleted Because Not authorized to delete Book");
    }

    @Override
    public Response sendRequestForApproval(Long bookId, String token) {
        UserModel userModel = CommonUtility.validateUser(token);
        if (RoleType.SELLER.equals(userModel.getRoleType())) {
            Optional<BookModel> book = bookRepository.findById(bookId);
            if (book.isPresent()) {
                book.get().setIsSendForApproval(true);
                bookRepository.save(book.get());
                return new Response(HttpStatus.OK.value(), "Book Approval request is send Successfully ");
            } else
                return new Response(HttpStatus.OK.value(), "Invalid book details");
        }
        return new Response(HttpStatus.OK.value(), "Unauthorized User");
    }

    @Override
    public List<BookModel> getNewlyAddedBooks(String token) {
        UserModel userModel = CommonUtility.validateUser(token);
        if (RoleType.SELLER.equals(userModel.getRoleType())) {
            List<BookModel> bookList = userModel.getBooks();
            List<BookModel> newlyAddedBooksList = new ArrayList<>();
            for (BookModel books : bookList) {
                if (books.getIsSendForApproval() == false) {
                    newlyAddedBooksList.add(books);
                }
            }
            return newlyAddedBooksList;
        }
        throw new UserException("Unauthorized", 200);
    }

    @Override
    public List<BookModel> getDisapprovedBooks(String token) {
        long id = JwtGenerator.decodeJWT(token);
        SellerModel seller = sellerRepository.getSeller(id).get();
        List<BookModel> bookList = seller.getBook();
        List<BookModel> disapprovedBooksList = new ArrayList<>();
        for (BookModel books : bookList) {
            if (books.getIsDisApproved() == true && books.getIsSendForApproval() == true) {
                disapprovedBooksList.add(books);
            }
        }
        return disapprovedBooksList;
    }

    @Override
    public List<BookModel> getApprovedBooks(String token) {
        long id = JwtGenerator.decodeJWT(token);
        SellerModel seller = sellerRepository.getSeller(id).get();
        List<BookModel> bookList = seller.getBook();
        List<BookModel> approvedBooksList = new ArrayList<>();
        for (BookModel books : bookList) {
            if (books.isVerfied() == true) {
                approvedBooksList.add(books);
            }
        }
        return approvedBooksList;
    }

    @Override
    public List<BookModel> getAllBooks(String token) throws UserException {
        long id = JwtGenerator.decodeJWT(token);
        SellerModel seller = sellerRepository.getSeller(id).get();
        return seller.getBook();
    }

    @Override
    public List<BookModel> getUnverfiedBooks(Long sellerId) {
        List<BookModel> book = bookRepository.getAllUnverfiedBooks(sellerId);
        return book;
    }

    @Override
    public List<SellerModel> getAllSellers() {
        List<SellerModel> sellers = sellerRepository.findAll();
        return sellers;
    }

}
