package com.bridgelabz.bookstore.dto;

import lombok.Data;

@Data
public class CartDto {
	private Long bookId;
	private int quantity;
	private double totalPrice;

	private String name;
	private String author;
	private String imgUrl;
	private int maxQuantity;
}
