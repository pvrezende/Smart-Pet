package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.StoreRequest;
import com.paulo.smartpet.dto.StoreResponse;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public List<StoreResponse> list() {
        return storeRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StoreResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    public StoreResponse create(StoreRequest request) {
        String code = normalizeCode(request.code());

        if (code != null && storeRepository.existsByCode(code)) {
            throw new BusinessException("Já existe loja cadastrada com este código");
        }

        Store store = new Store();
        store.setId(null);
        store.setName(request.name().trim());
        store.setCode(code);
        store.setAddress(normalizeBlank(request.address()));
        store.setPhone(cleanNumber(request.phone()));
        store.setActive(true);

        return toResponse(storeRepository.save(store));
    }

    public StoreResponse update(Long id, StoreRequest request) {
        Store store = getEntityById(id);
        String code = normalizeCode(request.code());

        if (code != null && storeRepository.existsByCodeAndIdNot(code, id)) {
            throw new BusinessException("Já existe outra loja cadastrada com este código");
        }

        store.setName(request.name().trim());
        store.setCode(code);
        store.setAddress(normalizeBlank(request.address()));
        store.setPhone(cleanNumber(request.phone()));

        return toResponse(storeRepository.save(store));
    }

    public void deactivate(Long id) {
        Store store = getEntityById(id);
        store.setActive(false);
        storeRepository.save(store);
    }

    public Store ensureDefaultStoreExists() {
        return storeRepository.findByCode("MATRIZ")
                .orElseGet(() -> {
                    Store store = new Store();
                    store.setName("Loja Matriz");
                    store.setCode("MATRIZ");
                    store.setAddress("");
                    store.setPhone("");
                    store.setActive(true);
                    return storeRepository.save(store);
                });
    }

    public Store getEntityById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja não encontrada"));
    }

    private StoreResponse toResponse(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getCode(),
                store.getAddress(),
                store.getPhone(),
                store.getActive()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String cleanNumber(String value) {
        return value == null || value.isBlank() ? null : value.replaceAll("\\D", "");
    }

    private String normalizeCode(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}