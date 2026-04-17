package com.paulo.smartpet.service;

import com.paulo.smartpet.dto.CreateExternalOrderRequest;
import com.paulo.smartpet.dto.ExternalOrderItemRequest;
import com.paulo.smartpet.dto.ExternalOrderItemResponse;
import com.paulo.smartpet.dto.ExternalOrderResponse;
import com.paulo.smartpet.dto.IntegrationSaleRequest;
import com.paulo.smartpet.entity.ExternalOrder;
import com.paulo.smartpet.entity.ExternalOrderItem;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.entity.Sale;
import com.paulo.smartpet.entity.Store;
import com.paulo.smartpet.exception.BusinessException;
import com.paulo.smartpet.exception.ResourceNotFoundException;
import com.paulo.smartpet.repository.ExternalOrderRepository;
import com.paulo.smartpet.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ExternalOrderService {

    private final ExternalOrderRepository externalOrderRepository;
    private final ProductRepository productRepository;
    private final StoreService storeService;
    private final SaleService saleService;

    public ExternalOrderService(
            ExternalOrderRepository externalOrderRepository,
            ProductRepository productRepository,
            StoreService storeService,
            SaleService saleService
    ) {
        this.externalOrderRepository = externalOrderRepository;
        this.productRepository = productRepository;
        this.storeService = storeService;
        this.saleService = saleService;
    }

    public List<ExternalOrderResponse> list(Long storeId, String status) {
        Store store = storeService.resolveStore(storeId);
        String normalizedStatus = normalizeStatus(status);

        List<ExternalOrder> orders = normalizedStatus == null
                ? externalOrderRepository.findByStoreIdOrderByCreatedAtDesc(store.getId())
                : externalOrderRepository.findByStoreIdAndStatusOrderByCreatedAtDesc(store.getId(), normalizedStatus);

        return orders.stream().map(this::toResponse).toList();
    }

    public ExternalOrderResponse getById(Long id) {
        return toResponse(getEntityById(id));
    }

    @Transactional
    public ExternalOrderResponse create(CreateExternalOrderRequest request) {
        Store store = storeService.resolveStore(request.storeId());
        String source = normalizeRequired(request.source(), "Origem é obrigatória");
        String externalId = normalizeRequired(request.externalId(), "Identificador externo é obrigatório");

        if (externalOrderRepository.existsByStoreIdAndSourceAndExternalId(store.getId(), source, externalId)) {
            ExternalOrder existing = externalOrderRepository.findByStoreIdAndSourceAndExternalId(store.getId(), source, externalId)
                    .orElseThrow(() -> new BusinessException("Pedido externo já registrado para esta loja"));

            return toResponse(existing);
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("Pedido externo deve possuir itens");
        }

        ExternalOrder order = new ExternalOrder();
        order.setStore(store);
        order.setSource(source);
        order.setExternalId(externalId);
        order.setStatus("PENDING");
        order.setCustomerName(request.customerName().trim());
        order.setCustomerDocument(normalizeBlank(request.customerDocument()));
        order.setCustomerPhone(cleanNumber(request.customerPhone()));
        order.setPaymentMethod(request.paymentMethod().trim());
        order.setDiscount(request.discount() == null ? BigDecimal.ZERO : request.discount());
        order.setNotes(normalizeBlank(request.notes()));

        BigDecimal total = BigDecimal.ZERO;

        for (ExternalOrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

            if (product.getStore() == null || !product.getStore().getId().equals(store.getId())) {
                throw new BusinessException("Produto não pertence à loja informada");
            }

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessException("Produto inativo não pode entrar no pedido: " + product.getName());
            }

            BigDecimal unitPrice = itemRequest.unitPrice() != null
                    ? itemRequest.unitPrice()
                    : BigDecimal.valueOf(product.getSalePrice());

            ExternalOrderItem item = new ExternalOrderItem();
            item.setExternalOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity())));

            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }

        if (order.getDiscount().compareTo(total) > 0) {
            throw new BusinessException("Desconto não pode ser maior que o total do pedido");
        }

        order.setTotalAmount(total);
        order.setFinalAmount(total.subtract(order.getDiscount()));

        return toResponse(externalOrderRepository.save(order));
    }

    @Transactional
    public ExternalOrderResponse confirm(Long id) {
        ExternalOrder order = getEntityById(id);

        if ("CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            return toResponse(order);
        }

        if ("CANCELED".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException("Pedido cancelado não pode ser confirmado");
        }

        IntegrationSaleRequest saleRequest = new IntegrationSaleRequest(
                null,
                order.getStore().getId(),
                order.getSource(),
                order.getExternalId(),
                order.getItems().stream()
                        .map(item -> new com.paulo.smartpet.dto.SaleItemRequest(item.getProductId(), item.getQuantity()))
                        .toList(),
                order.getPaymentMethod(),
                order.getDiscount(),
                order.getNotes()
        );

        com.paulo.smartpet.dto.SaleDetailsResponse saleResponse = saleService.createFromIntegration(saleRequest);
        Sale sale = saleService.getEntityById(saleResponse.id());

        order.setStatus("CONFIRMED");
        order.setSale(sale);

        return toResponse(externalOrderRepository.save(order));
    }

    @Transactional
    public ExternalOrderResponse cancel(Long id) {
        ExternalOrder order = getEntityById(id);

        if ("CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException("Pedido já confirmado não pode ser cancelado por este endpoint");
        }

        order.setStatus("CANCELED");
        return toResponse(externalOrderRepository.save(order));
    }

    public ExternalOrder getEntityById(Long id) {
        return externalOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido externo não encontrado"));
    }

    private ExternalOrderResponse toResponse(ExternalOrder order) {
        return new ExternalOrderResponse(
                order.getId(),
                order.getStore() != null ? order.getStore().getId() : null,
                order.getStore() != null ? order.getStore().getName() : null,
                order.getSource(),
                order.getExternalId(),
                order.getStatus(),
                order.getCustomerName(),
                order.getCustomerDocument(),
                order.getCustomerPhone(),
                order.getTotalAmount(),
                order.getDiscount(),
                order.getFinalAmount(),
                order.getPaymentMethod(),
                order.getNotes(),
                order.getSale() != null ? order.getSale().getId() : null,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream().map(this::toItemResponse).toList()
        );
    }

    private ExternalOrderItemResponse toItemResponse(ExternalOrderItem item) {
        return new ExternalOrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeBlank(value);
        if (normalized == null) {
            throw new BusinessException(message);
        }
        return normalized.toUpperCase();
    }

    private String normalizeStatus(String value) {
        String normalized = normalizeBlank(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String cleanNumber(String value) {
        return value == null || value.isBlank() ? null : value.replaceAll("\\D", "");
    }
}