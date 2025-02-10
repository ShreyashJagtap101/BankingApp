package com.account_service.services;

import com.account_service.entity.Account;
import java.util.List;

public interface AccountService {
    Account create(Account account);
    List<Account> getAccounts();
    Account getAccount(Long id);
    List<Account> getAccountByCustomerId(Long customerId);
    Account updateAccount(Long id, Account account);
    Account addBalance(Long id, int amount, Long customerId);
    Account withdrawBalance(Long id, int amount, Long customerId);
    void delete(Long id);
    void deleteAccountUsingCustomerId(Long customerId);
    void deleteAllAccounts();
}
