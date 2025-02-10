package com.customer_management_service.services.impl;

import com.customer_management_service.entites.Customer;
import com.customer_management_service.exceptions.ResourceNotFoundException;
import com.customer_management_service.repositories.CustomerRepository;
import com.customer_management_service.services.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;


@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Override
    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer get(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with given id not found"));
        return customer;
    }

    @Override
    public Customer update(Long id, Customer customer) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with given id not found"));
        existingCustomer.setName(customer.getName());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setAddress(customer.getAddress());
        return customerRepository.save(existingCustomer);
    }

    @Override
    public void delete(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with given id not found"));

        try {
            restTemplate.delete("http://ACCOUNT-SERVICE/account/user/" + customer.getCustomerId());
        } catch (Exception e) {
            logger.error("Failed to delete account from ACCOUNT-SERVICE for customer ID: {}", customer.getCustomerId(), e);
        }

        customerRepository.delete(customer);
    }
}