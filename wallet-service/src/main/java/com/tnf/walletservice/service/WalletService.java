package com.tnf.walletservice.service;

import com.tnf.walletservice.dto.CreateWalletRequest;
import com.tnf.walletservice.dto.TransferResponse;
import com.tnf.walletservice.dto.WalletResponse;
import com.tnf.walletservice.entity.Provider;
import com.tnf.walletservice.entity.Wallet;
import com.tnf.walletservice.exception.InsufficientBalanceException;
import com.tnf.walletservice.exception.InvalidAmountException;
import com.tnf.walletservice.exception.MaxLimitExceededException;
import com.tnf.walletservice.exception.WalletNotFoundException;
import com.tnf.walletservice.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Create a wallet for a customer. {@code maxLimit} is mandatory for PAYTM and
     * ignored for PHONEPE. The customer is trusted here — owner validation via
     * Customer Service is a deliberate future seam (see design notes).
     */
    public WalletResponse create(CreateWalletRequest request) {
        BigDecimal maxLimit = null;
        if (request.provider() == Provider.PAYTM) {
            if (request.maxLimit() == null) {
                throw new IllegalArgumentException("maxLimit is required for PAYTM wallets");
            }
            if (request.maxLimit().signum() < 0) {
                throw new IllegalArgumentException("maxLimit may not be negative");
            }
            maxLimit = request.maxLimit();
        }

        Wallet wallet = new Wallet(request.customerId(), request.provider(), maxLimit);
        Wallet saved = walletRepository.save(wallet);
        return WalletResponse.from(saved);
    }

    /**
     * List every wallet owned by a customer.
     */
    public List<WalletResponse> listByCustomer(String customerId) {
        return walletRepository.findByCustomerId(customerId).stream()
                .map(WalletResponse::from)
                .toList();
    }

    /**
     * Add money (Add Money). For PAYTM, the resulting balance may not exceed
     * {@code maxLimit}; when it would, the credit is rejected.
     */
    public WalletResponse add(String walletId, BigDecimal amount) {
        requirePositive(amount);
        Wallet wallet = findWallet(walletId);

        BigDecimal newBalance = wallet.getBalance().add(amount);
        rejectIfOverPaytmLimit(wallet, newBalance);

        wallet.setBalance(newBalance);
        return WalletResponse.from(walletRepository.save(wallet));
    }

    /**
     * Pay a bill (Pay Bill). Rejected if it would overdraw the balance.
     */
    public WalletResponse pay(String walletId, BigDecimal amount) {
        requirePositive(amount);
        Wallet wallet = findWallet(walletId);
        debit(wallet, amount);
        return WalletResponse.from(walletRepository.save(wallet));
    }

    /**
     * Transfer money to another wallet (Transfer to Wallet). Debits the source
     * and credits the target; the source may not be overdrawn, and a PAYTM target
     * may not be credited beyond its {@code maxLimit}.
     */
    public TransferResponse transfer(String walletId, String targetWalletId, BigDecimal amount) {
        requirePositive(amount);
        if (walletId.equals(targetWalletId)) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }

        Wallet source = findWallet(walletId);
        Wallet target = findWallet(targetWalletId);

        BigDecimal newTargetBalance = target.getBalance().add(amount);
        rejectIfOverPaytmLimit(target, newTargetBalance);
        debit(source, amount);
        target.setBalance(newTargetBalance);

        Wallet savedSource = walletRepository.save(source);
        Wallet savedTarget = walletRepository.save(target);
        return new TransferResponse(WalletResponse.from(savedSource), WalletResponse.from(savedTarget));
    }

    private Wallet findWallet(String walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("No wallet found with id " + walletId));
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0");
        }
    }

    private void debit(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet " + wallet.getId());
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
    }

    private void rejectIfOverPaytmLimit(Wallet wallet, BigDecimal newBalance) {
        if (wallet.getProvider() == Provider.PAYTM
                && wallet.getMaxLimit() != null
                && newBalance.compareTo(wallet.getMaxLimit()) > 0) {
            // Legacy behaviour posted a log to the Notification Service here
            // ("Wallet has exceeded the maximum amount"). That outbound call is a
            // future seam; for now the breach is rejected and logged locally.
            log.warn("Wallet {} has exceeded the maximum amount (limit={}, attempted balance={})",
                    wallet.getId(), wallet.getMaxLimit(), newBalance);
            throw new MaxLimitExceededException("Wallet has exceeded the maximum amount");
        }
    }
}
