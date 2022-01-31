package com.bridgelabz.bookstore.exception;


import lombok.Data;

@Data
public class UserException extends RuntimeException {
    private String message;
    int statusCode;
    Object data;

    public UserException(String message, int status, Object data) {
        this.message = message;
        this.statusCode = status;
        this.data = data;
    }

    public UserException(String message, int status) {
        this.message = message;
        this.statusCode = status;
    }
}
