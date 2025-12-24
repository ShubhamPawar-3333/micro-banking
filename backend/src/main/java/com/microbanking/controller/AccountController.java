package com.microbanking.controller;

import com.microbanking.dto.AccountDto;
import com.microbanking.entity.Account;
import com.microbanking.entity.User;
import com.microbanking.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<Map<String, List<AccountDto>>> getAccounts(@AuthenticationPrincipal User user) {
        List<AccountDto> accounts = accountService.getAccountsByUserId(user.getId());
        return ResponseEntity.ok(Map.of("accounts", accounts));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable UUID accountId) {
        AccountDto account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody Map<String, String> request) {
        Account.AccountType accountType = Account.AccountType.valueOf(request.get("accountType"));
        AccountDto account = accountService.createAccount(user, accountType);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Map<String, String>> closeAccount(
            @AuthenticationPrincipal User user,
            @PathVariable UUID accountId) {
        accountService.closeAccount(accountId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Account closed successfully"));
    }
}
