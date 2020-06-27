package com.bridgelabz.bookstore.model;


import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import lombok.Data;


@Entity
@Table(name = "AdminTable")
@Data
public class AdminModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_id")
	private Long adminId;
	
	private String adminName;
	
	private String emailId;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "seller_id")
	private List<SellerModel> sellers;
	
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private List<UserModel> users;
}
