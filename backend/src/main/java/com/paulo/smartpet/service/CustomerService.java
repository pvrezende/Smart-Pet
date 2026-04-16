package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CustomerRequest;
import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> list(Boolean active, String search) {
        String normalizedSearch = normalizeBlank(search);

        if (active != null && normalizedSearch == null) {
            return customerRepository.findByActiveOrderByNameAsc(active);
        }

        if (normalizedSearch == null) {
            return customerRepository.findByActiveTrueOrderByNameAsc();
        }

        String numericSearch = cleanNumber(normalizedSearch);

        List<Customer> result = new ArrayList<>();
        result.addAll(customerRepository.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(normalizedSearch));
        result.addAll(customerRepository.findByActiveTrueAndEmailContainingIgnoreCaseOrderByNameAsc(normalizedSearch));

        if (!numericSearch.isBlank()) {
            result.addAll(customerRepository.findByActiveTrueAndCpfContainingOrderByNameAsc(numericSearch));
            result.addAll(customerRepository.findByActiveTrueAndPhoneContainingOrderByNameAsc(numericSearch));
        }

        Map<Long, Customer> unique = new LinkedHashMap<>();
        for (Customer customer : result) {
            unique.put(customer.getId(), customer);
        }

        return unique.values()
                .stream()
                .sorted(Comparator.comparing(Customer::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
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
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}