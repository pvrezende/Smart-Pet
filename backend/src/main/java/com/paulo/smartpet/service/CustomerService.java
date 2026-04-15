package com.paulo.smartpet.service;

import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> list() {
        return customerRepository.findByActiveTrueOrderByNameAsc();
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public Customer create(Customer customer) {
        customer.setId(null);
        customer.setCpf(cleanNumber(customer.getCpf()));
        customer.setPhone(cleanNumber(customer.getPhone()));
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    public Customer update(Long id, Customer payload) {
        Customer customer = getById(id);
        customer.setName(payload.getName());
        customer.setCpf(cleanNumber(payload.getCpf()));
        customer.setPhone(cleanNumber(payload.getPhone()));
        customer.setEmail(payload.getEmail());
        customer.setAddress(payload.getAddress());
        return customerRepository.save(customer);
    }

    public void deactivate(Long id) {
        Customer customer = getById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    private String cleanNumber(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }
}
