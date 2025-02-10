package com.account_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name="Accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    private String accountType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date accountOpeningDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastActivity;

    private int balance;

    private Long customerId; // Ensure customerId is of type Long

    @Transient
    private Customer customer;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Date getAccountOpeningDate() {
        return accountOpeningDate;
    }

    public void setAccountOpeningDate(Date accountOpeningDate) {
        this.accountOpeningDate = accountOpeningDate;
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Account(String accountType, Date accountOpeningDate, Date lastActivity, int balance, Long customerId, Customer customer) {
        this.accountType = accountType;
        this.accountOpeningDate = accountOpeningDate;
        this.lastActivity = lastActivity;
        this.balance = balance;
        this.customerId = customerId;
        this.customer = customer;
    }

    public Account() {
    }
}
