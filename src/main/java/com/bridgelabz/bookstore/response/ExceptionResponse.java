package com.bridgelabz.bookstore.response;

import lombok.Data;

@Data
public class ExceptionResponse {

	String message;
	int StatusCode;
	Object data;
	
	public ExceptionResponse() {
		
	}
}
