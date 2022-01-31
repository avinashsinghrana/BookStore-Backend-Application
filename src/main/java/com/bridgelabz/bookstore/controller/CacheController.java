package com.bridgelabz.bookstore.controller;

import com.bridgelabz.bookstore.cache.EmailTemplateCache;
import com.bridgelabz.bookstore.exception.BookException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheController {

    @Autowired
    private EmailTemplateCache emailTemplateCache;

    @ApiOperation(value = "reload cache")
    @GetMapping("/reload")
    public ResponseEntity<String> reload() {
        emailTemplateCache.reload();
        return ResponseEntity.status(HttpStatus.OK).body("cache reloaded");
    }
}
