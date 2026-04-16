package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CustomerRequest;
import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
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
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    public Customer create(CustomerRequest request) {
        String cpf = cleanNumber(request.cpf());
        String phone = cleanNumber(request.phone());

        if (customerRepository.existsByCpf(cpf)) {
            throw new BusinessException("Já existe cliente cadastrado com este CPF");
        }

        Customer customer = new Customer();
        customer.setId(null);
        customer.setName(request.name().trim());
        customer.setCpf(cpf);
        customer.setPhone(phone);
        customer.setEmail(normalizeBlank(request.email()));
        customer.setAddress(normalizeBlank(request.address()));
        customer.setActive(true);

        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerRequest request) {
        Customer customer = getById(id);

        String cpf = cleanNumber(request.cpf());
        String phone = cleanNumber(request.phone());

        if (customerRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new BusinessException("Já existe outro cliente cadastrado com este CPF");
        }

        customer.setName(request.name().trim());
        customer.setCpf(cpf);
        customer.setPhone(phone);
        customer.setEmail(normalizeBlank(request.email()));
        customer.setAddress(normalizeBlank(request.address()));

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

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}