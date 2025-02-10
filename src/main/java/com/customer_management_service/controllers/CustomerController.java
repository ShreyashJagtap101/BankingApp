package com.customer_management_service.controllers;


import com.customer_management_service.entites.Customer;
import com.customer_management_service.payloads.ApiResponse;
import com.customer_management_service.services.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@Validated
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer createdCustomer = customerService.create(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    // Get all
    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers(){
        return ResponseEntity.ok(customerService.getAll());
    }

    // Get one
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long customerId){

        return ResponseEntity.status(HttpStatus.OK).body(customerService.get(customerId));

    }

    //delete
    @DeleteMapping("/{customerId}")
    public ApiResponse deleteCustomer(@PathVariable Long customerId)
    {

        this.customerService.delete(customerId);
        return new ApiResponse(" Customer is Successfully Deleted !!", true);
    }

    //update
    @PutMapping("/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@RequestBody Customer customer, @PathVariable Long customerId)
    {

        return ResponseEntity.status(HttpStatus.OK).body(customerService.update(customerId, customer));

    }

}
