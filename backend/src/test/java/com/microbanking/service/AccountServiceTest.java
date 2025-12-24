package com.microbanking.service;

import com.microbanking.dto.AccountDto;
import com.microbanking.entity.Account;
import com.microbanking.entity.User;
import com.microbanking.exception.ResourceNotFoundException;
import com.microbanking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        testAccount = Account.builder()
                .id(accountId)
                .user(testUser)
                .accountNumber("1234567890")
                .accountType(Account.AccountType.SAVINGS)
                .balance(BigDecimal.valueOf(1000))
                .status(Account.AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Should return accounts for user")
    void getAccountsByUserId_ShouldReturnAccounts() {
        when(accountRepository.findByUserId(userId)).thenReturn(List.of(testAccount));

        List<AccountDto> result = accountService.getAccountsByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("1234567890");
        verify(accountRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return account by ID")
    void getAccountById_ShouldReturnAccount() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        AccountDto result = accountService.getAccountById(accountId);

        assertThat(result.getId()).isEqualTo(accountId);
        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should throw exception when account not found")
    void getAccountById_ShouldThrowWhenNotFound() {
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");
    }

    @Test
    @DisplayName("Should create new account")
    void createAccount_ShouldCreateSuccessfully() {
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountDto result = accountService.createAccount(testUser, Account.AccountType.SAVINGS);

        assertThat(result).isNotNull();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Should close account")
    void closeAccount_ShouldCloseSuccessfully() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.closeAccount(accountId, userId);

        verify(accountRepository).save(argThat(account -> account.getStatus() == Account.AccountStatus.CLOSED));
    }

    @Test
    @DisplayName("Should throw security exception when closing other user's account")
    void closeAccount_ShouldThrowWhenNotOwner() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> accountService.closeAccount(accountId, UUID.randomUUID()))
                .isInstanceOf(SecurityException.class);
    }
}
