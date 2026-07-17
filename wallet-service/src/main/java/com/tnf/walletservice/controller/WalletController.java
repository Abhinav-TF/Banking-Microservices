package com.tnf.walletservice.controller;

import com.tnf.walletservice.dto.AmountRequest;
import com.tnf.walletservice.dto.CreateWalletRequest;
import com.tnf.walletservice.dto.TransferRequest;
import com.tnf.walletservice.dto.TransferResponse;
import com.tnf.walletservice.dto.WalletResponse;
import com.tnf.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /** Create a wallet. */
    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(request));
    }

    /** List a customer's wallets. */
    @GetMapping
    public List<WalletResponse> listByCustomer(@RequestParam String customerId) {
        return walletService.listByCustomer(customerId);
    }

    /** Add money to a wallet. */
    @PostMapping("/{id}/add")
    public WalletResponse add(@PathVariable String id, @Valid @RequestBody AmountRequest request) {
        return walletService.add(id, request.amount());
    }

    /** Pay a bill from a wallet. */
    @PostMapping("/{id}/pay")
    public WalletResponse pay(@PathVariable String id, @Valid @RequestBody AmountRequest request) {
        return walletService.pay(id, request.amount());
    }

    /** Transfer money to another wallet. */
    @PostMapping("/{id}/transfer")
    public TransferResponse transfer(@PathVariable String id, @Valid @RequestBody TransferRequest request) {
        return walletService.transfer(id, request.targetWalletId(), request.amount());
    }
}
