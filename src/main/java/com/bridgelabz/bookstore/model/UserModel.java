package com.bridgelabz.bookstore.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.bridgelabz.bookstore.enums.RoleType;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.NotAudited;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @NotNull
    private String fullName;

    @NotNull
    @Column(unique = true)
    private String emailId;

    @NotNull
    private String mobileNumber;

    @NotNull
    private String password;

    @Column(columnDefinition = "boolean default false")
    private boolean isVerified;

    @Temporal(TemporalType.TIMESTAMP)
    public Date registeredAt;

    @Temporal(TemporalType.TIMESTAMP)
    public Date updatedAt;

    @Column(columnDefinition = "boolean default false")
    public boolean userStatus;

    @Column
    private String profileUrl;

//	@ManyToMany(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
//	@JoinTable(name = "userbooks", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns ={@JoinColumn(name = "book_id") })
//	private List<BookModel> book;

    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    private List<BookModel> books;

    @Column(nullable = false, name = "role_type", columnDefinition = "ADMIN,SELLER,USER")
    @Enumerated(value = EnumType.STRING)
    private RoleType roleType;

    @NotAudited
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public List<UserDetails> userDetails;

    @NotAudited
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public List<CartModel> cartModels;

    public List<UserDetails> getListOfUserDetails() {
        return userDetails;
    }

    public void addUserDetails(UserDetails userDetail) {
        this.userDetails.add(userDetail);
    }

    public void removeUserDetails(UserDetails userDetail) {
        this.userDetails.remove(userDetail);
    }

    public UserModel(String fullName, String emailId, String mobileNumber, String password) {
        super();
        this.fullName = fullName;
        this.password = password;
        this.mobileNumber = mobileNumber;
        this.emailId = emailId;
    }

}
