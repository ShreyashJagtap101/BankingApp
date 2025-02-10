package com.customer_management_service.services;

import com.customer_management_service.entites.Customer;

import java.util.List;

public interface CustomerService {

    Customer create(Customer customer);
    List<Customer> getAll();
    Customer get(Long id);
    Customer update(Long id, Customer customer);
    void delete(Long id);
}