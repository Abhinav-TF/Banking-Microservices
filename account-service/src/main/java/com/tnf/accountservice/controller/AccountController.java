package com.tnf.accountservice.controller;

import com.tnf.accountservice.entity.Account;
import com.tnf.accountservice.entity.Amount;
import com.tnf.accountservice.entity.Transfer;
import com.tnf.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account savedAccount = service.createAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Account getAccountById(@PathVariable String id) { return service.getAccountById(id); }

    @GetMapping
    public ResponseEntity<List<Account>> getAccountsByCustomerId(@RequestParam String customerId){
        List<Account> accounts = service.getAccountsByCustomer(customerId);
        if (accounts.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<String> deposit(@PathVariable String id, @RequestBody Amount amount){
        String tx = service.deposit(id, amount.getAmount());
        if(tx != null)
            return ResponseEntity.status(HttpStatus.OK).body(tx);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<String> withdraw(@PathVariable String id, @RequestBody Amount amount){
        String tx = service.withdraw(id, amount.getAmount());
        if(tx != null)
            return ResponseEntity.status(HttpStatus.OK).body(tx);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody Transfer transferObj){
        String tx = service.transfer(transferObj);
        if(tx != null)
            return ResponseEntity.status(HttpStatus.OK).body(tx);
        return ResponseEntity.noContent().build();
    }
    
}
