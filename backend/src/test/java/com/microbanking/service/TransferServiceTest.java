package com.microbanking.service;

import com.microbanking.dto.TransactionDto;
import com.microbanking.dto.TransferRequest;
import com.microbanking.entity.Account;
import com.microbanking.entity.Transaction;
import com.microbanking.entity.User;
import com.microbanking.exception.InsufficientFundsException;
import com.microbanking.exception.ResourceNotFoundException;
import com.microbanking.repository.AccountRepository;
import com.microbanking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Account fromAccount;
    private Account toAccount;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .build();

        fromAccount = Account.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .accountNumber("1111111111")
                .balance(BigDecimal.valueOf(1000))
                .build();

        toAccount = Account.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .accountNumber("2222222222")
                .balance(BigDecimal.valueOf(500))
                .build();
    }

    @Test
    @DisplayName("Should transfer funds successfully")
    void transfer_ShouldSucceed() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setAmount(BigDecimal.valueOf(200));
        request.setDescription("Test transfer");

        when(accountRepository.findByIdWithLock(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TransactionDto result = transferService.transfer(request, userId);

        assertThat(result).isNotNull();
        assertThat(fromAccount.getBalance()).isEqualTo(BigDecimal.valueOf(800));
        assertThat(toAccount.getBalance()).isEqualTo(BigDecimal.valueOf(700));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw when source account not found")
    void transfer_ShouldThrowWhenSourceNotFound() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(UUID.randomUUID());
        request.setToAccountId(UUID.randomUUID());
        request.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findByIdWithLock(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Source account not found");
    }

    @Test
    @DisplayName("Should throw when insufficient funds")
    void transfer_ShouldThrowWhenInsufficientFunds() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setAmount(BigDecimal.valueOf(2000)); // More than balance

        when(accountRepository.findByIdWithLock(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(toAccount.getId())).thenReturn(Optional.of(toAccount));

        assertThatThrownBy(() -> transferService.transfer(request, userId))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    @DisplayName("Should throw when accessing other user's account")
    void transfer_ShouldThrowWhenNotOwner() {
        TransferRequest request = new TransferRequest();
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());
        request.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findByIdWithLock(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByIdWithLock(toAccount.getId())).thenReturn(Optional.of(toAccount));

        assertThatThrownBy(() -> transferService.transfer(request, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class);
    }
}
