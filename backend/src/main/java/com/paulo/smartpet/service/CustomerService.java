package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CustomerRequest;
import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.entity.Store;
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
    private final StoreService storeService;

    public CustomerService(CustomerRepository customerRepository, StoreService storeService) {
        this.customerRepository = customerRepository;
        this.storeService = storeService;
    }

    public List<Customer> list(Long storeId, Boolean active, String search) {
        Store store = storeService.resolveStore(storeId);
        String normalizedSearch = normalizeBlank(search);

        if (active != null && normalizedSearch == null) {
            return customerRepository.findByStoreIdAndActiveOrderByNameAsc(store.getId(), active);
        }

        if (normalizedSearch == null) {
            return customerRepository.findByStoreIdAndActiveTrueOrderByNameAsc(store.getId());
        }

        String numericSearch = cleanNumber(normalizedSearch);

        List<Customer> result = new ArrayList<>();
        result.addAll(customerRepository.findByStoreIdAndActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(store.getId(), normalizedSearch));
        result.addAll(customerRepository.findByStoreIdAndActiveTrueAndEmailContainingIgnoreCaseOrderByNameAsc(store.getId(), normalizedSearch));

        if (!numericSearch.isBlank()) {
            result.addAll(customerRepository.findByStoreIdAndActiveTrueAndCpfContainingOrderByNameAsc(store.getId(), numericSearch));
            result.addAll(customerRepository.findByStoreIdAndActiveTrueAndPhoneContainingOrderByNameAsc(store.getId(), numericSearch));
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
        Store store = storeService.resolveStore(request.storeId());
        String cpf = cleanNumber(request.cpf());
        String phone = cleanNumber(request.phone());

        if (customerRepository.existsByStoreIdAndCpf(store.getId(), cpf)) {
            throw new BusinessException("Já existe cliente cadastrado com este CPF nesta loja");
        }

        Customer customer = new Customer();
        customer.setId(null);
        customer.setName(request.name().trim());
        customer.setCpf(cpf);
        customer.setPhone(phone);
        customer.setEmail(normalizeBlank(request.email()));
        customer.setAddress(normalizeBlank(request.address()));
        customer.setStore(store);
        customer.setActive(true);

        return customerRepository.save(customer);
    }

    public Customer update(Long id, CustomerRequest request) {
        Customer customer = getById(id);
        Long effectiveStoreId = request.storeId() != null ? request.storeId() : (customer.getStore() != null ? customer.getStore().getId() : null);
        Store store = storeService.resolveStore(effectiveStoreId);

        String cpf = cleanNumber(request.cpf());
        String phone = cleanNumber(request.phone());

        if (customerRepository.existsByStoreIdAndCpfAndIdNot(store.getId(), cpf, id)) {
            throw new BusinessException("Já existe outro cliente cadastrado com este CPF nesta loja");
        }

        customer.setName(request.name().trim());
        customer.setCpf(cpf);
        customer.setPhone(phone);
        customer.setEmail(normalizeBlank(request.email()));
        customer.setAddress(normalizeBlank(request.address()));
        customer.setStore(store);

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