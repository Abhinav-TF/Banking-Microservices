package com.tnf.transactionservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @GetMapping
    public ResponseEntity<String> transactions() {
        return ResponseEntity.ok("transactions");
    }

}
