package com.bridgelabz.bookstore.serviceimplementation;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.SellerModel;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.BookRepository;
import com.bridgelabz.bookstore.repository.SellerRepository;
import com.bridgelabz.bookstore.repository.UserRepository;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    @Transactional
    public Response addBook(BookDto newBook, String token) throws UserException {

        Long id = JwtGenerator.decodeJWT(token);
        String role = userRepository.checkRole(id);
        Optional<UserModel> user = userRepository.findById(id);
        if (role.equals("SELLER")) {
            BookModel book = new BookModel();
            BeanUtils.copyProperties(newBook, book);
            book.setBookImgUrl(newBook.getBookImgUrl());
            book.setIsDisApproved(false);
            book.setIsSendForApproval(false);
            SellerModel seller = sellerRepository.getSellerByEmailId(user.get().getEmailId()).get();
            book.setSellerId(seller.getSellerId());
            BookModel books = bookRepository.save(book);
            seller.getBook().add(books);
            sellerRepository.save(seller);
            return new Response(environment.getProperty("book.verification.status"), HttpStatus.OK.value(), book);

        } else {
            throw new UserException(environment.getProperty("book.unauthorised.status"), HttpStatus.FORBIDDEN.value());
        }

    }

     @Override
    @Transactional
    public Response updateBook(UpdateBookDto newBook, String token, Long bookId) throws UserException {
        long id = JwtGenerator.decodeJWT(token);
        String role = userRepository.checkRole(id);
        if (role.equals("SELLER")) {
            Optional<BookModel> book = bookRepository.findById(bookId);
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
            SellerModel seller = new SellerModel();
            seller.getBook().add(book.get());
            //	elasticSearchService.updateBook(book.get());
            return new Response(HttpStatus.OK.value(), "Book update Successfully Need to Verify");

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
    @Transactional
    public Response deleteBook(String token, Long bookId) {
        long id = JwtGenerator.decodeJWT(token);
        String role = userRepository.checkRole(id);
        if (role.equals("SELLER") || role.equals("ADMIN")) {
            bookRepository.deleteById(bookId);
            //elasticSearchService.deleteNote(bookId);
            return new Response(HttpStatus.OK.value(), "Book deleted Successfully ");

        }
        return new Response(HttpStatus.OK.value(), "Book Not deleted Becoz Not Authoriized to delete Book");
    }

     @Override
    @Transactional
    public Response sendRequestForApproval(Long bookId, String token) {
        long id = JwtGenerator.decodeJWT(token);
        String role = userRepository.checkRole(id);
        if (role.equals("SELLER")) {
            Optional<BookModel> book = bookRepository.findById(bookId);
            book.get().setIsSendForApproval(true);
            bookRepository.save(book.get());
            return new Response(HttpStatus.OK.value(), "Book Approval request is send Successfully ");
        }
        return new Response(HttpStatus.OK.value(), "Unauthorized User");
    }

     @Override
    @Transactional
    public List<BookModel> getNewlyAddedBooks(String token) {
        long id = JwtGenerator.decodeJWT(token);
        SellerModel seller = sellerRepository.getSeller(id).get();
        List<BookModel> bookList = seller.getBook();
        List<BookModel> newlyAddedBooksList = new ArrayList<>();
        for (BookModel books : bookList) {
            if (books.getIsSendForApproval() == false) {
                newlyAddedBooksList.add(books);
            }
        }
        return newlyAddedBooksList;
    }

     @Override
    @Transactional
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
    @Transactional
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
    @Transactional
    public List<BookModel> getAllBooks(String token) throws UserException {
        long id = JwtGenerator.decodeJWT(token);
        SellerModel seller = sellerRepository.getSeller(id).get();
        return seller.getBook();
    }

     @Override
    @Transactional
    public List<BookModel> getUnverfiedBooks(Long sellerId) {
        List<BookModel> book = bookRepository.getAllUnverfiedBooks(sellerId);
        return book;
    }

    @Override
    @Transactional
    public List<SellerModel> getAllSellers() {
        List<SellerModel> sellers = sellerRepository.findAll();
        return sellers;
    }

}
