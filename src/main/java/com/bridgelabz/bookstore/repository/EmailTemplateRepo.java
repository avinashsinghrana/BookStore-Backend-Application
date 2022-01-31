package com.bridgelabz.bookstore.repository;

import com.bridgelabz.bookstore.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepo extends JpaRepository<EmailTemplate, Long> {
}
