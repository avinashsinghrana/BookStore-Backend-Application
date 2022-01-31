package com.bridgelabz.bookstore.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.awt.print.Book;
import java.time.LocalDateTime;
@Data
public class BookException extends Exception {
    private String message;
    int statusCode;
    Object data;

    public BookException(String message, int status, Object data) {
        this.message = message;
        this.statusCode=status;
        this.data = data;
    }

    public BookException(String message,int status) {
        this.message = message;
        this.statusCode=status;
    }
}
