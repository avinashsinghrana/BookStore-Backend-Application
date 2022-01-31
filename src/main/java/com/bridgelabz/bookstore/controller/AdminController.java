package com.bridgelabz.bookstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.model.BookModel;
import com.bridgelabz.bookstore.response.Response;
import com.bridgelabz.bookstore.service.AdminService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/admin")
@CrossOrigin(allowedHeaders = "*", origins = "*")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@ApiOperation("Get Books For Verification")
	@GetMapping("/getBooksForVerification/{sellerId}")
	public ResponseEntity<Response> getAllUnverifiedBooks(@RequestHeader("token") String token,@PathVariable Long sellerId)
			throws UserNotFoundException {
		List<BookModel> book = adminService.getAllUnVerifiedBooks(token,sellerId);
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new Response("Getting all the books which are unverified", 200, book));
	}

	@ApiOperation("Book For Verification")
	@PutMapping("/bookVerification/{bookId}")
	public ResponseEntity<Response> bookVerification(@PathVariable("bookId") Long bookId,
			@RequestHeader("token") String token) throws Exception {

		Response verifiedBook = adminService.bookVerification(bookId,token);
		return new ResponseEntity<Response>(verifiedBook, HttpStatus.OK);
	}

	@ApiOperation("Book For Unverification")
	@PutMapping("/bookUnVerification/{bookId}")
	public ResponseEntity<Response> bookUnVerification(@PathVariable("bookId") Long bookId, @RequestHeader("token") String token) throws Exception {

		Response verifiedBook = adminService.bookUnVerification(bookId, token);
		return new ResponseEntity<Response>(verifiedBook, HttpStatus.OK);
	}
}