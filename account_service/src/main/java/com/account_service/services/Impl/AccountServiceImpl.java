package com.account_service.services.Impl;

import com.account_service.entity.Account;
import com.account_service.entity.Customer;
import com.account_service.exceptions.ResourceNotFoundException;
import com.account_service.repositories.AccountRepository;
import com.account_service.services.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Date;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Account create(Account account) {
        String url = "http://CUSTOMER-SERVICE/customers/" + account.getCustomerId();
        try {
            logger.info("Checking customer in CUSTOMER-SERVICE: {}", url);
            ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResourceNotFoundException("Customer with given ID not found. Cannot create account.");
            }

            // ✅ Proceed with account creation
            Date currentDate = new Date();
            account.setAccountOpeningDate(currentDate);
            account.setLastActivity(currentDate);
            return accountRepository.save(account);

        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Customer with given ID not found.");
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Error connecting to CUSTOMER-SERVICE.");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }


    @Override
    public List<Account> getAccounts() {
        List<Account> accounts = accountRepository.findAll();

        // ✅ Fetch customer details for each account
        for (Account account : accounts) {
            String url = "http://CUSTOMER-SERVICE/customers/" + account.getCustomerId();
            try {
                ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    account.setCustomer(response.getBody());
                } else {
                    account.setCustomer(null);
                }
            } catch (Exception e) {
                account.setCustomer(null); // Handle errors gracefully
            }
        }
        return accounts;
    }


    @Override
    public Account getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with given ID not found"));

        // ✅ Fetch customer details from CUSTOMER-SERVICE
        String url = "http://CUSTOMER-SERVICE/customers/" + account.getCustomerId();
        try {
            logger.info("Calling CUSTOMER-SERVICE: {}", url);
            ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                account.setCustomer(response.getBody());  // ✅ Set the customer object in the account
            } else {
                logger.warn("Customer service returned non-OK status: {}", response.getStatusCode());
                account.setCustomer(null);
            }
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Customer not found in CUSTOMER-SERVICE: {}", url);
            account.setCustomer(null);
        } catch (ResourceAccessException e) {
            logger.error("CUSTOMER-SERVICE is unreachable: {}", url);
            account.setCustomer(null);
        } catch (Exception e) {
            logger.error("Error fetching customer details: {}", e.getMessage());
            account.setCustomer(null);
        }

        return account;
    }


    @Override
    public List<Account> getAccountByCustomerId(Long customerId) {
        // ✅ Fetch accounts linked to the customer ID
        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found for customer ID: " + customerId);
        }

        // ✅ Fetch customer details from CUSTOMER-SERVICE
        String url = "http://CUSTOMER-SERVICE/customers/" + customerId;
        try {
            ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Customer customer = response.getBody();
                accounts.forEach(account -> account.setCustomer(customer)); // ✅ Set customer details in each account
            } else {
                accounts.forEach(account -> account.setCustomer(null));
            }
        } catch (Exception e) {
            accounts.forEach(account -> account.setCustomer(null)); // Handle errors gracefully
        }

        return accounts;
    }


    @Override
    public Account updateAccount(Long id, Account updatedAccount) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with given ID not found. Try again with correct details!"));

        // ✅ Update fields only if they are provided
        if (updatedAccount.getAccountType() != null) {
            existingAccount.setAccountType(updatedAccount.getAccountType());
        }
        if (updatedAccount.getBalance() != 0) {
            existingAccount.setBalance(updatedAccount.getBalance());
        }

        // ✅ Update lastActivity to current time
        existingAccount.setLastActivity(new Date());

        // ✅ Save the updated account
        Account savedAccount = accountRepository.save(existingAccount);

        // ✅ Fetch and set customer details from CUSTOMER-SERVICE
        String url = "http://CUSTOMER-SERVICE/customers/" + savedAccount.getCustomerId();
        try {
            ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                savedAccount.setCustomer(response.getBody());
            } else {
                savedAccount.setCustomer(null);
            }
        } catch (Exception e) {
            savedAccount.setCustomer(null);  // Handle errors gracefully
        }

        return savedAccount;
    }

    @Override
    public Account addBalance(Long id, int amount, Long customerId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with given ID not found."));

        // ✅ Ensure the customer exists
        String url = "http://CUSTOMER-SERVICE/customers/" + customerId;
        Customer customer = null;

        try {
            ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                customer = response.getBody();
            }
        } catch (Exception e) {
            customer = null; // Handle failure gracefully
        }

        // ✅ Add the amount to the balance
        account.setBalance(account.getBalance() + amount);
        account.setLastActivity(new Date());

        // ✅ Save the updated account
        Account updatedAccount = accountRepository.save(account);

        // ✅ Set customer details before returning
        updatedAccount.setCustomer(customer);

        return updatedAccount;
    }


    @Override
    public Account withdrawBalance(Long id, int amount, Long customerId) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with given ID not found."));

        // ✅ Ensure customer exists
        String url = "http://CUSTOMER-SERVICE/customers/" + customerId;
        Customer customer = null;

        try {
            ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                customer = response.getBody();
            }
        } catch (Exception e) {
            customer = null; // Handle failure gracefully
        }

        // ✅ Ensure sufficient balance before withdrawing
        if (account.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance for withdrawal.");
        }

        // ✅ Deduct the amount from balance
        account.setBalance(account.getBalance() - amount);
        account.setLastActivity(new Date());

        // ✅ Save the updated account
        Account updatedAccount = accountRepository.save(account);

        // ✅ Set customer details before returning
        updatedAccount.setCustomer(customer);

        return updatedAccount;
    }

    @Override
    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with given ID not found!"));

        accountRepository.delete(account);
        logger.info("Deleted account with ID: {}", id);
    }

    @Override
    public void deleteAccountUsingCustomerId(Long customerId) {
        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found for the given customer ID.");
        }

        accountRepository.deleteAll(accounts);
        logger.info("Deleted {} accounts for customer ID: {}", accounts.size(), customerId);
    }

    @Override
    public void deleteAllAccounts() {
        long count = accountRepository.count();
        if (count == 0) {
            throw new ResourceNotFoundException("No accounts found to delete.");
        }

        accountRepository.deleteAll();
        logger.info("Deleted all {} accounts from the database.", count);
    }
}