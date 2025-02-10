package com.account_service.controller;

import com.account_service.entity.Account;
import com.account_service.payload.ApiResponse;
import com.account_service.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account createdAccount = accountService.create(account);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }


    // Get single Account Details
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable Long accountId) {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccount(accountId));
    }

    // Get Accounts using Customer ID
    @GetMapping("/user/{customerId}")
    public ResponseEntity<List<Account>> getAccountsUsingCustomerID(@PathVariable Long customerId)
    {
        return ResponseEntity.ok(accountService.getAccountByCustomerId(customerId));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.getAccounts();
        return ResponseEntity.ok(accounts);
    }

    // update account
    @PutMapping("/{accountID}")
    public ResponseEntity<Account> updateAccount(@RequestBody Account account, @PathVariable Long accountID){

        return ResponseEntity.status(HttpStatus.OK).body(accountService.updateAccount(accountID,account));
    }

    // Add Money
    @PutMapping("/addmoney/{accountID}")
    public ResponseEntity<Account> addMoney(@PathVariable Long accountID,@RequestParam int amount,  @RequestParam Long customerId)
    {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.addBalance(accountID,amount, customerId));
    }

    // withdraw Money
    @PutMapping("/withdraw/{accountID}")
    public ResponseEntity<Account> withdraw(@PathVariable Long accountID,@RequestParam int amount, @RequestParam Long customerId)
    {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.withdrawBalance(accountID,amount, customerId));
    }

    // Delete Account
    @DeleteMapping("/{accountId}")
    public ApiResponse deleteAccount(@PathVariable Long accountId)
    {
        this.accountService.delete(accountId);
        return new ApiResponse("Account is Successfully Deleted", true);
    }

    // Delete Account using customerId
    @DeleteMapping("/user/{customerId}")
    public ApiResponse deleteAccountByUserId(@PathVariable Long customerId)
    {
        this.accountService.deleteAccountUsingCustomerId(customerId);
        return new ApiResponse(" Accounts with given userId is deleted Successfully", true);

    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllAccounts() {
        accountService.deleteAllAccounts();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
