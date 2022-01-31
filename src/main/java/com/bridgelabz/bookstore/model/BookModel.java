package com.bridgelabz.bookstore.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "book")
@Data
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "book_history")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookModel {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "BookName is mandatory")
    private String bookName;

    private int quantity;

    @Min(1)
    private Double price;

    @Size(min = 2, max = 30)
    private String authorName;

    @CreationTimestamp
    private LocalDateTime createdDateAndTime;

    @UpdateTimestamp
    private LocalDateTime UpdatedDateAndTime;

    @Column
    @NotNull
    private String bookDetails;

    @Column(nullable = false)
    private boolean isVerfied;

    private Boolean isDisApproved;

    private Boolean isSendForApproval;

    @Column(name = "rejection_count", columnDefinition = "int default 0")
    private int rejectionCount;

    private String rejectionReason;

    @NotAudited

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WishListModel> wishListModel;

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserModel user;

    @Column
    private String bookImgUrl;

    @NotAudited
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    public List<WishListModel> wishListModels;

    @NotAudited
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    public List<OrderPlaced> orderPlaceds;
}