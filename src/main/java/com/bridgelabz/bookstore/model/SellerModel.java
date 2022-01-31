package com.bridgelabz.bookstore.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "seller")
@Data
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "seller_history")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SellerModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@NotBlank(message="SellerName is mandatory")
	private String sellerName;

	@Email
	@Column(name = "email_id")
	private String emailId;

	@Column(name = "user_id")
	private long userId;

    private String imgUrl;
	@ManyToMany(cascade = {CascadeType.ALL,CascadeType.MERGE},fetch=FetchType.LAZY)
	@JoinTable(name = "sellerbooks", joinColumns = { @JoinColumn(name = "seller_id") }, inverseJoinColumns ={@JoinColumn(name = "book_id") })
	private List<BookModel> book = new ArrayList<>();;
}
