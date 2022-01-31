package com.bridgelabz.bookstore.model;

import com.bridgelabz.bookstore.enums.MailStatus;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "email_template")
public class EmailTemplate {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 200000, name = "template")
    private String template;

    @Column(name = "subject")
    private String subject;

    @Column(name = "body")
    private String body;

    @Column(name = "informer")
    private String informer;

    @Column(name = "cc")
    private String cc;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "enum('ACTIVE','INACTIVE') default 'INACTIVE'")
    private MailStatus status;

    @Column(name = "name")
    private String name;
}
