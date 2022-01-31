package com.bridgelabz.bookstore.controller;

import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.model.SellerModel;
import com.bridgelabz.bookstore.model.UserModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bridgelabz.bookstore.dto.BookDto;
import com.bridgelabz.bookstore.dto.UpdateBookDto;
import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.SellerService;
import com.bridgelabz.bookstore.serviceimplementation.AmazonS3ClientServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/sellers")
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class SellerController {

	@Autowired
	private SellerService sellerService;

	@Autowired
	private AmazonS3ClientServiceImpl amazonS3ClientService;

	@PostMapping(value = "/addBook")
	public ResponseEntity<Response> addBook(@RequestBody BookDto newBook, @RequestHeader("token") String token)
			throws UserException {
		Response addedBook = sellerService.addBook(newBook, token);
		return new ResponseEntity<Response>(addedBook, HttpStatus.OK);
	}

	@GetMapping("/getUnverifiedBooks")
	public ResponseEntity<Response> getAllBooks(@RequestHeader("token") String token) throws UserException {
		List<BookModel> book = sellerService.getAllBooks(token);
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new Response("Getting all the books which are unverified", 200, book));
	}

	@PostMapping("/uploadFile")
	public ResponseEntity<Response> uploadFile(@RequestParam("file") MultipartFile file) {
		String url = amazonS3ClientService.uploadFile(file);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Uploaded successfully", 200, url));
	}

	@PutMapping(value = "/updateBook/{bookId}")
	public ResponseEntity<Response> updateBook(@RequestBody @Valid UpdateBookDto newBook,
			@RequestHeader("token") String token, @PathVariable("bookId") Long bookId) throws UserException {
		sellerService.updateBook(newBook, token, bookId);
		return new ResponseEntity<Response>(HttpStatus.OK);
	}

	@DeleteMapping(value = "/deleteBook/{bookId}")
	public ResponseEntity<Response> deleteBook(@RequestHeader("token") String token,
			@PathVariable("bookId") Long bookId) throws UserException {
		sellerService.deleteBook(token, bookId);
		return new ResponseEntity<Response>(HttpStatus.OK);
	}

	@GetMapping("/getUnverifiedBooksOfSeller/{sellerId}")
	public ResponseEntity<Response> getunverifiedBooks(@PathVariable Long sellerId) {
		List<BookModel> book = sellerService.getUnverfiedBooks(sellerId);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Got all un verified Books", 200, book));
	}

	@GetMapping("/getAllSellers")
	public ResponseEntity<Response> getAllSellers() {
		List<SellerModel> book = sellerService.getAllSellers();
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Get all Sellers", 200, book));
	}
	@PutMapping(value = "/sendApprovalRequest/{bookId}")
	public ResponseEntity<Response> sendApprovalRequest(@RequestHeader("token") String token,
											   @PathVariable("bookId") Long bookId) {
		  Response response = sellerService.sendRequestForApproval(bookId,token);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

	@GetMapping("/getNewlyAddedBooks")
	public ResponseEntity<Response> getNewlyAddedBooks(@RequestHeader("token") String token)
	{
		List<BookModel> book= sellerService.getNewlyAddedBooks(token);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Getting Newly Added Books", 200,book));
	}

	@GetMapping("/getDisapprovedBooks")
	public ResponseEntity<Response> getDisapprovedBooks(@RequestHeader("token") String token)
	{
		List<BookModel> book= sellerService.getDisapprovedBooks(token);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Getting Disapproved Books", 200,book));
	}
	@GetMapping("/getApprovedBooks")
	public ResponseEntity<Response> getApprovedBooks(@RequestHeader("token") String token)
	{
		List<BookModel> book= sellerService.getApprovedBooks(token);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(new Response("Getting Approved Books", 200,book));
	}
}
