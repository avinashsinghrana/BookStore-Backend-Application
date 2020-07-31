package com.bridgelabz.bookstore.response;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private String email;
	private String subject;
	private String message;
	private String msgSign;

	public EmailObject(String email, String subject, String message, String msgSign) {
		this.email = email;
		this.subject = subject;
		this.message = message;
		this.msgSign = msgSign;
	}
}
