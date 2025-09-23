package com.app.musicstore.service;

import com.app.musicstore.model.Customer;
import com.app.musicstore.model.User;
import com.app.musicstore.repository.CustomerRepository;
import com.app.musicstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> findByUserId(Long userId) {
        return customerRepository.findByUserId(userId);
    }

    public Customer updateCustomerPreferences(Long userId, String preferences) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        customer.setPreferences(preferences);
        return customerRepository.save(customer);
    }
}
