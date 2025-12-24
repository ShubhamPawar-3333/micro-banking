package com.microbanking.service;

import com.microbanking.dto.TransactionDto;
import com.microbanking.dto.TransferRequest;
import com.microbanking.entity.Account;
import com.microbanking.entity.Transaction;
import com.microbanking.exception.InsufficientFundsException;
import com.microbanking.exception.ResourceNotFoundException;
import com.microbanking.repository.AccountRepository;
import com.microbanking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionDto transfer(TransferRequest request, UUID userId) {
        // Validate accounts
        Account fromAccount = accountRepository.findByIdWithLock(request.getFromAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        Account toAccount = accountRepository.findByIdWithLock(request.getToAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        // Validate ownership
        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied to source account");
        }

        // Validate balance
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for this transfer");
        }

        // Perform transfer
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.COMPLETED)
                .referenceNumber(generateReferenceNumber())
                .description(request.getDescription())
                .build();

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        Transaction saved = transactionRepository.save(transaction);

        return toDto(saved);
    }

    public Page<TransactionDto> getTransactionsByUserId(UUID userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    public Page<TransactionDto> getTransactionsByAccountId(UUID accountId, Pageable pageable) {
        return transactionRepository.findByAccountId(accountId, pageable).map(this::toDto);
    }

    private String generateReferenceNumber() {
        return "TXN-" + LocalDateTime.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .fromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null)
                .toAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getId() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
