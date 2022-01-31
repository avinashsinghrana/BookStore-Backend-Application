package com.bridgelabz.bookstore.model;

import lombok.Data;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Data
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@AuditTable(value = "wishlist_history")
@Table(name = "wishlist")
@Entity
public class WishListModel {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private long userId;
    private long bookId;
    private String bookName;
    private String authorName;
    private String bookImgUrl;
    private double price;

    @NotAudited
    @JoinColumn(name = "wishListModels", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private BookModel book;
}
