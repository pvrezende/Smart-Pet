package com.paulo.smartpet;

import com.paulo.smartpet.entity.Customer;
import com.paulo.smartpet.entity.Product;
import com.paulo.smartpet.repository.CustomerRepository;
import com.paulo.smartpet.repository.ProductRepository;
import com.paulo.smartpet.service.CompanySettingsService;
import com.paulo.smartpet.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SmartPetApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPetApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            UserService userService,
            CompanySettingsService companySettingsService
    ) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(new Product(null, "Royal Canin Adult", "cao", "Royal Canin", 15.0, 70.0, 89.90, 12, 5, "7891000100101", true));
                productRepository.save(new Product(null, "Whiskas Adulto Peixe", "gato", "Whiskas", 3.0, 18.0, 24.50, 8, 5, "7891000100102", true));
                productRepository.save(new Product(null, "Pedigree Adulto Carne", "cao", "Pedigree", 20.0, 50.0, 65.00, 15, 5, "7891000100103", true));
                productRepository.save(new Product(null, "Golden Gatos Castrados", "gato", "Golden", 10.1, 60.0, 78.90, 6, 5, "7891000100104", true));
            }

            if (customerRepository.count() == 0) {
                customerRepository.save(new Customer(null, "Cliente Padrão", "00000000000", "27999999999", "cliente@exemplo.com", "", true));
            }

            userService.ensureDefaultAdminExists();
            companySettingsService.ensureDefaultExists();
        };
    }
}